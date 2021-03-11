package org.apache.james.webapi.app.controller;

import org.apache.james.webapi.app.security.JamesUserSevice;
import org.apache.james.webapi.model.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1/users")
public class UserController {

	@Autowired
	JamesUserSevice userService;

	@PostMapping("/add")
	public ResponseEntity<ResponseMessage> login(@RequestBody LoginVO vo) {
		userService.add(vo.getUsername(), vo.getPassword());
		return ResponseEntity.ok(ResponseMessage.success());
	}



}
