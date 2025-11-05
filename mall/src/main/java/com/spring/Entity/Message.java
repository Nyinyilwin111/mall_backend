package com.spring.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String content; //  text message

    private String filePath; // added: stores only file path, not binary

    private LocalDateTime timeStamp;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ElementCollection
    private Set<UUID> readBy = new HashSet<>();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Message other)) return false;
        return Objects.equals(content, other.getContent())
                && Objects.equals(filePath, other.getFilePath())
                && Objects.equals(timeStamp, other.getTimeStamp())
                && Objects.equals(user, other.getUser())
                && Objects.equals(chat, other.getChat());
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, filePath, timeStamp, user, chat);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", filePath='" + filePath + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
