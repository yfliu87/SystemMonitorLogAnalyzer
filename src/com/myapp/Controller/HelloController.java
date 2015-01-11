package com.myapp.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HelloController {

	@RequestMapping(value="/hello",method=RequestMethod.GET)
	public String printHello(ModelMap model){
		model.addAttribute("message", "hello spring mvc framework!");
		return "hello";
	}
	
	/*
	 * get parameter passed from address bar
	 * localhost:8080/HelloWeb/hellouser?username=yifei
	 * 
	 * pass parameter to view using model
	 */
	@RequestMapping("/hellouser")
	public String hello(String username, Model model){
		model.addAttribute("username", username);
		System.out.println(username);
		return "hello";
	}
}
