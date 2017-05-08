# Java + Spring 实现邮件的发送

有关邮件发送的详细内容和代码实现可参看网址：http://www.yiibai.com/javamail_api。本文只做入门介绍。

---

##**JavaMail API 介绍**
**JavaMail API**按其功能划分通常可分为如下三大类：
>* **Message类** ：创建和解析邮件的核心API，用于创建一封邮件，可以设置发件人，收件人，邮件主题，正文信息，发送时间等信息。
>* **Transport类**：发送邮件的核心API类
>* **Store类**：接收邮件的核心API类

**邮件相关协议：**
> **SMTP协议**: 发送邮件协议；
**POP3协议** :  获取邮件协议；
**IMAP** :  接收信息的高级协议；
**MIME** :  邮件拓展内容格式：信息格式，附件格式；

下图用于演示两帐户相互发送邮件的过程:
![Alt text](./1494207349714.png)

## 开启SMTP服务
因为本文只做如何发送邮件的介绍，发送邮件跟SMTP协议相关，所以在发送前需要保证所使用的邮件客户端开启了SMTP相关服务。本文采用的是QQ邮箱（也可以改用163邮箱)。
下面讲述下如何在qq邮箱中开启SMTP服务：
![Alt text](./1494210425219.png)

首先进入qq邮箱点击设置，在账户里可看到开启服务项，点击开启(照片中我已开启服务)，会弹出让你发送短信"**配置邮件客户端**"的提示框。
按照要求发送短信后点击"我已发送"，会接收到一个像下面图片中的授权码，这个授权码要记下来，在后续代码实现中的密码用到的就是这个授权码(不是qq账号密码)：
![Alt text](./1494210608537.png)

---
##**代码实现：**
## Java SE 发送Email
首先引入jar包：
下载Javax Mail的jar包https://java.net/projects/javamail/pages/Home 

下面是实现的代码：
```
package test2;

import java.util.Date;
import java.util.Properties;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JavaMailDemo {

	 // 发件人的 邮箱(qq邮箱)
    public static String senderEmailAccount = "1032335358@qq.com";
    //此为SMTP客户端的授权码
    public static String senderEmailPassword = "****";
    // 发件人邮箱的 SMTP 服务器地址
    public static String senderEmailSMTPHost = "smtp.qq.com";
    // 收件人邮箱（替换为自己知道的有效邮箱）
    public static String receiverMailAccount = "1032335358@qq.com";
    

    public static void main(String[] args) throws Exception {
    	
        // 1. 创建参数配置
        Properties props = new Properties();                   
        props.setProperty("mail.transport.protocol", "smtp");  
        props.setProperty("mail.smtp.host", senderEmailSMTPHost);  
        props.setProperty("mail.smtp.auth", "true");   
        // SMTP 服务器的端口
        final String smtpPort = "465";
        props.setProperty("mail.smtp.port", smtpPort);
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.socketFactory.port", smtpPort);

        // 2. 根据配置创建会话对象, 用于和邮件服务器交互
        // Get the Session object.
        Session session = Session.getInstance(props,
           new javax.mail.Authenticator() {
              protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication(senderEmailAccount, senderEmailPassword);
              }
           });
        session.setDebug(true);                                 // 设置为debug模式, 可以查看详细的发送 log

        // 4. 根据 Session 获取邮件传输对象
		Transport transport = null;
		try {
			// 3. 创建一封邮件
			MimeMessage message = createMimeMessage(session, senderEmailAccount, receiverMailAccount);

			transport = session.getTransport();

			// 5. 使用 邮箱账号 和 密码 连接邮件服务器
			transport.connect(senderEmailAccount, senderEmailPassword);

			// 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
			transport.sendMessage(message, message.getAllRecipients());
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			// 7. 关闭连接
			transport.close();
		}

    }

    /**
     * 创建一封只包含文本的简单邮件
     *
     * @param session 和服务器交互的会话
     * @param sendMail 发件人邮箱
     * @param receiveMail 收件人邮箱
     * @return
     * @throws Exception
     */
    public static MimeMessage createMimeMessage(Session session, String sendMail, String receiveMail) throws Exception {
        // 1. 创建一封邮件
        MimeMessage message = new MimeMessage(session);

        // 2. From: 发件人
        message.setFrom(new InternetAddress(sendMail, "QQ邮箱", "UTF-8"));

        // 3. To: 收件人（可以增加多个收件人、抄送、密送）
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMail, "1032335358@qq.com", "UTF-8"));

        // 4. Subject: 邮件主题
        message.setSubject("邮箱登录异常", "UTF-8");

        // 5. Content: 邮件正文（可以使用html标签）
        message.setContent("您好，您的QQ邮箱账号异常，请重新登录！", "text/html;charset=UTF-8");

        // 6. 设置发件时间
        message.setSentDate(new Date());

        // 7. 保存设置
        message.saveChanges();

        return message;
    }


}

```
程序运行成功以后，即可收到一封类似下图的邮件：
![Alt text](./1494211014299.png)

---
## **使用Spring发送Email**
Spring Email 抽象的核心是MailSender接口，MailSender的实现能够通过连接Email服务器实现邮件发送的功能。

Spring自带了一个MailSender的实现：JavaMailSenderImpl. 它会使用JavaMail API来发送Email。

下面是代码实现：
1. 首先引入必须的jar包：
```
 <dependency>
    <groupId>com.sun.mail</groupId>       
    <artifactId>javax.mail</artifactId>
    <version>1.5.5</version>
 </dependency>
```

2. 配置邮件发送器
```
package com.wgs.mailSender.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Created by wanggenshen_sx on 2017/5/8.
 */
@Service
public class MailSender implements InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(MailSender.class);
	private JavaMailSenderImpl mailSender;

	/**
	 * 发送邮件
	 * @param to
	 */
	public void sendMail(String to){
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		//发送方邮箱
		mailMessage.setFrom("1032335358@qq.com");
		//接收方邮箱
		mailMessage.setTo(to);
		//发送的邮件主题
		mailMessage.setSubject("Spring Mail Sender");
		//发送的邮件内容
		mailMessage.setText("这是用Spring框架发送的邮件!");

		mailSender.send(mailMessage);
	}

	/**
	 * 配置邮件发送器
	 * @throws Exception
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		mailSender = new JavaMailSenderImpl();
		//用户名
		mailSender.setUsername("1032335358@qq.com");
		//SMTP客户端的授权码(每个人这个密码不一样，密码与上面的username对应密码相同)
		mailSender.setPassword("**********");
		// 发件人邮箱的 SMTP 服务器地址
		mailSender.setHost("smtp.qq.com");
		//邮件服务器监听的端口
		mailSender.setPort(465);
		//协议SMTP+SSL
		mailSender.setProtocol("smtps");
		mailSender.setDefaultEncoding("utf8");
		Properties javaMailProperties = new Properties();
		javaMailProperties.put("mail.smtp.ssl.enable", true);
		mailSender.setJavaMailProperties(javaMailProperties);
	}
}

```

3. 邮件发送接口
写了一个简单邮件发送的接口，可用Postman进行测试：
```
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

```

4. 测试
运行项目后打开PostMan进行测试：
![Alt text](./1494229580953.png)

从返回的JSON字符串可以看出发送已经成功。
并且确实收到一份邮件:
![Alt text](./1494229630370.png)

































---
参考：http://blog.csdn.net/xietansheng/article/details/51673073







