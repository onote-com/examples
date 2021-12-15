package com.example.pizza;

import com.eventstore.dbclient.EventData;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificRecordBase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class AvroEventDataBuilder {
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final Encoder encoder;
    private byte[] eventData;
    private byte[] metadata;
    private String eventType;
    private boolean isJson;
    private UUID id;

    private <T extends SpecificRecordBase> AvroEventDataBuilder(T record, boolean isJson) {
        Encoder encoder = null;
        try {
            if (isJson) {
                encoder = EncoderFactory.get().jsonEncoder(record.getSchema(), out);
            } else {
                encoder = EncoderFactory.get().directBinaryEncoder(out, null);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.encoder = encoder;
        this.isJson = isJson;
        try {
            record.customEncode(encoder);
            encoder.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.eventData = out.toByteArray();
        out.reset();
    }

    public static <T extends SpecificRecordBase> AvroEventDataBuilder json(String eventType, T record) {
        return json(null, eventType, record);
    }

    public static <T extends SpecificRecordBase> AvroEventDataBuilder json(UUID id, String eventType, T record) {
        AvroEventDataBuilder self = new AvroEventDataBuilder(record, true);
        self.eventType = eventType;
        self.id = id;
        return self;
    }

    public static <T extends SpecificRecordBase> AvroEventDataBuilder binary(String eventType, T record) {
        return json(null, eventType, record);
    }

    public static <T extends SpecificRecordBase> AvroEventDataBuilder binary(UUID id, String eventType, T record) {
        AvroEventDataBuilder self = new AvroEventDataBuilder(record, false);
        self.eventType = eventType;
        self.id = id;
        return self;
    }


    public AvroEventDataBuilder eventId(UUID id) {
        this.id = id;
        return this;
    }

    public <T extends SpecificRecordBase> AvroEventDataBuilder metadata(T value) {
        try {
            value.customEncode(encoder);
            encoder.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.metadata = out.toByteArray();
        out.reset();

        return this;
    }

    public EventData build() {
        UUID eventId = this.id == null ? UUID.randomUUID() : this.id;
        String contentType = this.isJson ? "application/json" : "application/octet-stream";
        return new EventData(eventId, this.eventType, contentType, this.eventData, this.metadata);
    }
}
