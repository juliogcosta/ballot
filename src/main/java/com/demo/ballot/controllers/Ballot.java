package com.demo.ballot.controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.demo.ballot.Observables;

@RestController
public class Ballot implements ApplicationContextAware 
{
	@Autowired
	protected ApplicationContext context;

	@GetMapping(path = "/ballot/register-vote/{candidate}")
	public ResponseEntity<String> register(@PathVariable(name = "candidate", required = true) String candidate) 
	{
		Observables.getInstance().getCandidates().put(candidate, Observables.getInstance().getCandidates().get(candidate)+1);

		return ResponseEntity.ok().build();
	}

	@GetMapping(path = "/ballot/log")
	public ResponseEntity<String> log(@RequestHeader(name = "X-Secret", required = true) String secret)
	{
		if (secret == null || !secret.equals(Observables.getSecret())) 
		{
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		JSONObject candidates = new JSONObject(Observables.getInstance().getCandidates());

		return ResponseEntity.ok().body(candidates.toString(2).concat("\n\n"));
	}

	@GetMapping(path = "/ballot/shutdown")
	public ResponseEntity<String> shutdownContext(@RequestHeader(name = "X-Secret", required = true) String secret) throws IOException 
	{
		if (secret == null || !secret.equals(Observables.getSecret())) 
		{
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		new Timer().schedule(new TimerTask() 
		{
			@Override
			public void run()
			{
				try 
				{
					String filepath = "/tmp/ballot-01."; 

					File votes = new File(filepath.concat("votes"));

					File checksum = new File(filepath.concat("checksum"));

					if (votes.delete() && checksum.delete()) 
				    {
						String candidates = new JSONObject(Observables.getInstance().getCandidates()).toString(2);

				    	/*
				    	 * Criação do arquivo com o checksum
				    	 * 
				    	 */
				    	BufferedWriter buffer = new BufferedWriter(new FileWriter(filepath.concat("votes"), true));

				    	buffer.write(candidates);

				    	buffer.close();

				    	/*
				    	 * Criação do arquivo com o checksum
				    	 * 
				    	 */
						Checksum crc32 = new CRC32();

						crc32.update(candidates.getBytes(), 0, candidates.getBytes().length);

				    	buffer = new BufferedWriter(new FileWriter(filepath.concat("checksum"), true));

				    	buffer.write(String.valueOf(crc32.getValue()));

				    	buffer.close();
				    } 
				    else 
				    {
				    	throw new Exception("can't remove ballot's file");
				    }

					((ConfigurableApplicationContext) context).close();
				} 
				catch (Exception e) 
				{
					throw new RuntimeException(e.getMessage());
				}
			}
		}, 1200);

		return ResponseEntity.ok().body("Shutdown ok.\n\n");
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException 
	{
		this.context = context;
	}
}
