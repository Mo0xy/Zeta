package it.aruba.pec.zeta.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PecMessageDTO {

    private Long id;

    @JsonProperty("message_id")
    private String messageId;

    @NotBlank(message = "Mittente è obbligatorio")
    @Email(message = "Email mittente non valida")
    private String sender;

    @NotBlank(message = "Almeno un destinatario è obbligatorio")
    private List<String> recipients;

    @NotBlank(message = "Oggetto è obbligatorio")
    private String subject;

    private String body;

    @JsonProperty("html_body")
    private String htmlBody;

    private List<AttachmentDTO> attachments;

    private MessageStatus status;

    @JsonProperty("sent_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sentAt;

    @JsonProperty("received_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime receivedAt;

    @JsonProperty("delivery_receipt")
    private String deliveryReceipt;

    @JsonProperty("read_receipt")
    private String readReceipt;

    public enum MessageStatus {
        DRAFT,
        SENDING,
        SENT,
        DELIVERED,
        READ,
        FAILED,
        RECEIVED
    }
}