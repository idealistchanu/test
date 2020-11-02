package com.skmwizard.user.apis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
class PictureChangeRequest {
    @Schema(title = "파일 아이디", example = "5f895c2b46aab934e416e855")
    @NotBlank(message = "사진(FileId)을 입력하세요")
    private String picture;
}
