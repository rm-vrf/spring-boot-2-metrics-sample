package com.mydomain.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Random;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class DemoService {

	private static final Logger LOG = LoggerFactory.getLogger(DemoService.class);
	private Timer timer;
	private Counter count;
	private Counter size;
	private Counter ok;
	private Counter error;
	
	@Autowired
	private RestTemplate restTemplate;
	
	public DemoService(MeterRegistry registry) {
		timer = Timer.builder("copy.file.time").
				description("Copy file response time").
				register(registry);
		
		count = Counter.builder("copy.file.count").
				description("Copy file count").
				register(registry);
		
		size = Counter.builder("copy.file.size").
				description("Copy file size in bytes").
				register(registry);
		
		ok = Counter.builder("copy.file.ok").
				description("Copy file success").
				register(registry);
		
		error = Counter.builder("copy.file.error").
				description("Copy file fail").
				register(registry);
	}
	
	@Scheduled(fixedDelay = 3000)
	public void copyFile() throws Exception {
		
		long size = 0L;
		int count = 0;
		try {
			String tmp = System.getProperty("java.io.tmpdir");
			LOG.debug("copy file: {}", tmp);
			File file = new File(tmp);
			long beginTime = System.currentTimeMillis();
			File[] children = file.listFiles();
			for (File child : children) {
				LOG.debug("child: {}", child.getName());
				if (child.isFile() && new Random().nextInt(1000) > 600) {
					File newFile = new File("/dev/null");
					copyFile(child, newFile);
					size += child.length();
					count ++;
				}
			}
			Thread.sleep(new Random().nextInt(3000));
			long costTime = System.currentTimeMillis() - beginTime;
			
			LOG.info("copy.file.time: {}, copy.file.count: {}, copy.file.size: {}", costTime, count, size);
			
			timer.record(Duration.ofMillis(costTime));
			this.count.increment(count);
			this.size.increment(size);
			ok.increment();
		} catch (Exception e) {
			error.increment();
			throw e;
		}
	}
	
	public void baidu() throws InterruptedException {
		String html = restTemplate.getForObject("https://www.baidu.com", String.class);
		LOG.debug("html: {}", html);
	}
	
	private void copyFile(File srcFile, File newFile) throws IOException {
		if (!newFile.exists()) {
			newFile.createNewFile();
		}
		
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(srcFile);
			out = new FileOutputStream(newFile);
			IOUtils.copyLarge(in, out);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}
}
