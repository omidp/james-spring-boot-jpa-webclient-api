package org.apache.james.webapi.app.controller;

import org.apache.james.core.Domain;
import org.apache.james.domainlist.api.DomainListException;
import org.apache.james.domainlist.jpa.JPADomainList;
import org.apache.james.webapi.app.service.ServiceException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1/domains")
public class DomainController {

	@Autowired
	JPADomainList domainService;

	@PostMapping("/add")
	public ResponseEntity<ResponseMessage> login(@RequestBody String domainJson) {
		JSONObject json = new JSONObject(domainJson);
		if(json.has("domainName") == false)
			throw new ServiceException("domainName is not present");
		try {
			domainService.addDomain(Domain.of(json.getString("domainName")));
		} catch (JSONException | DomainListException e) {
			throw new ServiceException(e.getMessage());
		}
		return ResponseEntity.ok(ResponseMessage.success());
	}



}
