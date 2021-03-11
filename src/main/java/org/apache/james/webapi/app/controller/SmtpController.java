package org.apache.james.webapi.app.controller;

import org.apache.james.webapi.app.service.SmtpService;
import org.apache.james.webapi.model.SendMailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1")
public class SmtpController {

	@Autowired
	SmtpService smtpService;

	@PostMapping("/send")
	public ResponseEntity<ResponseMessage> send(@RequestBody SendMailVO vo) {
		smtpService.send(vo.getFrom(), vo.getTo(), vo.getSubject(), vo.getMsg());
		return ResponseEntity.ok(ResponseMessage.success());
	}

}
