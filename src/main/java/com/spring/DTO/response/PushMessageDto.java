package com.spring.DTO.response;

public class PushMessageDto {

    private String content;
    private String targetEmail;   // optional
    private String targetPhone;   // optional

    public PushMessageDto() {}
    public PushMessageDto(String content) { this.content = content; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTargetEmail() { return targetEmail; }
    public void setTargetEmail(String targetEmail) { this.targetEmail = targetEmail; }

    public String getTargetPhone() { return targetPhone; }
    public void setTargetPhone(String targetPhone) { this.targetPhone = targetPhone; }
}
