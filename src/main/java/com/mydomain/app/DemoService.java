package com.mydomain.app;

import java.io.File;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class DemoService {

	private static final Logger LOG = LoggerFactory.getLogger(DemoService.class);
	private Timer timer;
	private Counter counter;

	@Autowired
	private RestTemplate restTemplate;

	public DemoService(MeterRegistry registry) {

		timer = Timer.builder("sleep.time").register(registry);

		counter = Counter.builder("sleep.count").register(registry);

		Gauge.builder("free.disk.space", "/", path -> new File(path).getFreeSpace()).baseUnit("bytes")
				.register(registry);
	}

	@Scheduled(fixedDelay = 3000)
	public void sleep() throws Exception {
		int n = new Random().nextInt(3000);

		timer.record(() -> {
			try {
				Thread.sleep(n);
			} catch (InterruptedException e) {
				LOG.error("error", e);
			}
		});

		counter.increment();
	}

	public void baidu() throws InterruptedException {
		String html = restTemplate.getForObject("https://www.baidu.com", String.class);
		LOG.debug("html: {}", html);
	}

}
