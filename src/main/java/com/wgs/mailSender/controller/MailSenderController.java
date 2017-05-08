package com.wgs.mailSender.controller;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.wgs.mailSender.util.JSONUtil;
import com.wgs.mailSender.util.MailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by wanggenshen_sx on 2017/5/8.
 */
@Controller
public class MailSenderController {
	private static final Logger logger = LoggerFactory.getLogger(MailSenderController.class);

	@Autowired
	MailSender mailSender;

	@RequestMapping(path = {"/mail/mailSend"}, method = {RequestMethod.POST})
	@ResponseBody
	public String emailSend(){

		try {
			mailSender.sendMail("1032335358@qq.com");
			return JSONUtil.getJSONString(0, "SUCCESS");
		}catch (Exception e){
			logger.error("邮件发送失败: " + e.getMessage());
			return JSONUtil.getJSONString(1, "FAILED");
		}

	}
}
