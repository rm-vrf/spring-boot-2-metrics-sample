# spring-boot-2-metrics-sample

Spring Boot Actuator Starter 提供了很多生产环境功能，对线上环境的监控调试有很大作用。Spring Boot 2 比起以前的版本，管理端口发生了一些调整。

- 大部分管理端点在默认是关闭的，开发者需要自己打开。管理端点会暴露程序内部的很多细节，出于安全方面的考虑不用的话应该是关闭的；
- 管理端点的地址改为层级结构，访问 `/actuator` 端点可以看到打开的端点；
- 指标端点也发生了变化，调整为层级结构。指标数据支持标签，计数器和计时器提供了更多的细节。

## 指标端点

在 Spring Boot 项目中添加 Actuator Starter 依赖：

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

`/metrics` 端点默认是关闭的。为了演示方便程序里打开了所有端点，实际环境应该打开尽量少的的端点：

```shell
management.endpoints.web.exposure.include=*
```

启动程序，访问 `/actuator`，列出所有管理端点：

```shell
$ curl http://localhost:8080/actuator
{
    "_links":
    {
        "self":
        {
            "href": "http://localhost:8080/actuator",
            "templated": false
        },
        "auditevents":
        {
            "href": "http://localhost:8080/actuator/auditevents",
            "templated": false
        },
        "beans":
        {
            "href": "http://localhost:8080/actuator/beans",
            "templated": false
        },
        "health":
        {
            "href": "http://localhost:8080/actuator/health",
            "templated": false
        },
        "conditions":
        {
            "href": "http://localhost:8080/actuator/conditions",
            "templated": false
        },
        "configprops":
        {
            "href": "http://localhost:8080/actuator/configprops",
            "templated": false
        },
        "env":
        {
            "href": "http://localhost:8080/actuator/env",
            "templated": false
        },
        "env-toMatch":
        {
            "href": "http://localhost:8080/actuator/env/{toMatch}",
            "templated": true
        },
        "info":
        {
            "href": "http://localhost:8080/actuator/info",
            "templated": false
        },
        "loggers":
        {
            "href": "http://localhost:8080/actuator/loggers",
            "templated": false
        },
        "loggers-name":
        {
            "href": "http://localhost:8080/actuator/loggers/{name}",
            "templated": true
        },
        "heapdump":
        {
            "href": "http://localhost:8080/actuator/heapdump",
            "templated": false
        },
        "threaddump":
        {
            "href": "http://localhost:8080/actuator/threaddump",
            "templated": false
        },
        "metrics-requiredMetricName":
        {
            "href": "http://localhost:8080/actuator/metrics/{requiredMetricName}",
            "templated": true
        },
        "metrics":
        {
            "href": "http://localhost:8080/actuator/metrics",
            "templated": false
        },
        "scheduledtasks":
        {
            "href": "http://localhost:8080/actuator/scheduledtasks",
            "templated": false
        },
        "httptrace":
        {
            "href": "http://localhost:8080/actuator/httptrace",
            "templated": false
        },
        "mappings":
        {
            "href": "http://localhost:8080/actuator/mappings",
            "templated": false
        }
    }
}
```

打开 `/metrics` 端点：

```shell
$ curl http://localhost:8080/actuator/metrics
{
    "names":
    [
        "jvm.memory.max",
        "http.server.requests",
        "process.files.max",
        "jvm.gc.memory.promoted",
        "tomcat.cache.hit",
        "system.load.average.1m",
        "tomcat.cache.access",
        "jvm.memory.used",
        "jvm.gc.max.data.size",
        "jvm.gc.pause",
        "jvm.memory.committed",
        "system.cpu.count",
        "logback.events",
        "tomcat.global.sent",
        "jvm.buffer.memory.used",
        "tomcat.sessions.created",
        "jvm.threads.daemon",
        "system.cpu.usage",
        "jvm.gc.memory.allocated",
        "tomcat.global.request.max",
        "tomcat.global.request",
        "tomcat.sessions.expired",
        "jvm.threads.live",
        "jvm.threads.peak",
        "tomcat.global.received",
        "process.uptime",
        "tomcat.sessions.rejected",
        "process.cpu.usage",
        "tomcat.threads.config.max",
        "jvm.classes.loaded",
        "jvm.classes.unloaded",
        "tomcat.global.error",
        "tomcat.sessions.active.current",
        "tomcat.sessions.alive.max",
        "jvm.gc.live.data.size",
        "tomcat.servlet.request.max",
        "tomcat.threads.current",
        "tomcat.servlet.request",
        "process.files.open",
        "jvm.buffer.count",
        "jvm.buffer.total.capacity",
        "tomcat.sessions.active.max",
        "tomcat.threads.busy",
        "process.start.time",
        "tomcat.servlet.error"
    ]
}
```

返回的结果里面只有指标的名称。指标的值有更多的细节数据，在 `/metrics/{requiredMetricName}` 端点提供。调用一下 `/v1/test` 接口，这个接口内部对 `www.baidu.com` 发起请求：

```shell
$ curl http://localhost:8080/v1/test
Hello!
```

查看接口响应时间：

```shell
$ curl 'http://localhost:8080/actuator/metrics/http.server.requests?tag=uri:/v1/test'
{
    "name": "http.server.requests",
    "description": null,
    "baseUnit": "seconds",
    "measurements":
    [
        {
            "statistic": "COUNT",
            "value": 14
        },
        {
            "statistic": "TOTAL_TIME",
            "value": 2.454856112
        },        {
            "statistic": "MAX",
            "value": 0.077488588
        }
    ],
    "availableTags":
    [
        {
            "tag": "exception",
            "values":
            [
                "None"
            ]
        },
        {
            "tag": "status",
            "values":
            [
                "200"
            ]
        }
    ]
}
```

指标数据按照标签查询，提供的信息比以前更多。可以看到访问次数、总时长（用这两个数据可以计算出平均响应时间）、最大时间。

还有一个非常有用的功能：Actuator Starter 自动收集了 RestTemplate 的运行数据：

```shell
$ curl http://localhost:8080/actuator/metrics/http.client.requests
{
    "name": "http.client.requests",
    "description": "Timer of RestTemplate operation",
    "baseUnit": "seconds",
    "measurements":
    [
        {
            "statistic": "COUNT",
            "value": 14
        },
        {
            "statistic": "TOTAL_TIME",
            "value": 2.30982354
        },        {
            "statistic": "MAX",
            "value": 0
        }
    ],
    "availableTags":
    [
        {
            "tag": "method",
            "values":
            [
                "GET"
            ]
        },
        {
            "tag": "clientName",
            "values":
            [
                "www.baidu.com"
            ]
        },        {
            "tag": "uri",
            "values":
            [
                "/https://www.baidu.com"
            ]
        },
        {
            "tag": "status",
            "values":
            [
                "200"
            ]
        }
    ]
}
```

RestTemplate 的请求次数和响应时间都被记录下来了，不需要我们自己去埋点计时，这是自动的，要感谢 Spring Boot 的 Starter。所以当我们要依赖一个组件的时候，应该首先选择相应的 Starter，Starter 会为我们识别自动配置、创建Spring Bean、健康检查，还有统计运行指标。

## 自定义指标

示例程序里创建了 3 个指标，在构造函数里定义了计时器、计数器和度量值：

```java
public DemoService(MeterRegistry registry) {

	timer = Timer.builder("sleep.time").register(registry);

	counter = Counter.builder("sleep.count").register(registry);

	Gauge.builder("free.disk.space", "/", 
		path -> new File(path).getFreeSpace()).baseUnit("bytes")
				.register(registry);
}
```

`Gauge.Gauge.builder` 的参数是 `ToDoubleFunction`, 这里演示了统计磁盘剩余空间，单位是字节。

在业务代码里埋点，收集计时器和计数器：

```java
int n = new Random().nextInt(3000);

timer.record(() -> {
	try {
		Thread.sleep(n);
	} catch (InterruptedException e) {
		LOG.error("error", e);
	}
});

counter.increment();
```

这里演示的是最原始的埋点方式，想要干的更漂亮，可以使用 AOP、Proxy 技术。Spring Boot 1.x 的计数器每次只能累加 1，现在可以累加任意值，在 `Counter` 接口有这个重载函数：

```java
void increment(double amount);
```

如果我们要统计下载文件的字节数，用计数器就方便多了。

访问 `/metrics` 端点，可以看到我们自定义的指标：

```shell
$ curl http://localhost:8080/actuator/metrics
{
    "names":
    [
        ...
        "sleep.count",
        "sleep.time",
        "free.disk.space",
        ...
    ]
}
```

查看一下 `sleep.time` 指标，可以看到运行次数、总响应时间和最大响应时间：

```shell
$ curl http://localhost:8080/actuator/metrics/sleep.time
{
    "name": "sleep.time",
    "description": null,
    "baseUnit": "seconds",
    "measurements":
    [
        {
            "statistic": "COUNT",
            "value": 21
        },
        {
            "statistic": "TOTAL_TIME",
            "value": 27.113834878
        },        {
            "statistic": "MAX",
            "value": 2.650188095
        }
    ],
    "availableTags":
    [

    ]
}
```

## 收集和存储

Spring Boot 2.0 自动依赖和配置了 Micrometer, Micrometer 是一个指标收集器，支持多种监控系统，包括：

- AppOptics
- Atlas
- Datadog
- Dynatrace
- Elastic
- Ganglia
- Graphite
- Humio
- Influx
- JMX
- KairosDB
- New Relic
- Prometheus
- SignalFx
- Simple (in-memory)
- StatsD
- Wavefront

有一些系统支持目前还列在 Spring Boot 的开发计划中。示例程序演示了 Prometheus 收集方式。

Prometheus 使用 scrape 技术从运行程序中“拉”数据，比起程序向指标系统主动“推”数据，“拉”数据的方式更加灵活。运维人员可以根据自己的需要，建立自己的监控系统，从程序上拉自己关注的数据。

在项目里加上 Prometheus 的依赖：

```xml
<dependency>
	<groupId>io.micrometer</groupId>
	<artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

启动之后，打开 `/actuator/prometheus` 端点，可以看到 Prometheus 需要的数据就展现在这里：

```shell
$ curl http://localhost:8080/actuator/prometheus
# HELP jvm_threads_daemon The current number of live daemon threads
# TYPE jvm_threads_daemon gauge
jvm_threads_daemon 21.0
# HELP jvm_gc_max_data_size_bytes Max size of old generation memory pool
# TYPE jvm_gc_max_data_size_bytes gauge
jvm_gc_max_data_size_bytes 1.431830528E9
# HELP tomcat_sessions_created_total
# TYPE tomcat_sessions_created_total counter
tomcat_sessions_created_total 0.0
# HELP jvm_classes_loaded The number of classes that are currently loaded in the Java virtual machine
# TYPE jvm_classes_loaded gauge
jvm_classes_loaded 7744.0
# HELP tomcat_servlet_error_total
# TYPE tomcat_servlet_error_total counter
tomcat_servlet_error_total{name="default",} 0.0
# HELP tomcat_cache_access_total
# TYPE tomcat_cache_access_total counter
tomcat_cache_access_total 0.0
# HELP tomcat_threads_config_max
# TYPE tomcat_threads_config_max gauge
tomcat_threads_config_max{name="http-nio-8080",} 200.0
# HELP jvm_buffer_count An estimate of the number of buffers in the pool
# TYPE jvm_buffer_count gauge
jvm_buffer_count{id="direct",} 11.0
jvm_buffer_count{id="mapped",} 0.0
# HELP process_cpu_usage The "recent cpu usage" for the Java Virtual Machine process
# TYPE process_cpu_usage gauge
process_cpu_usage 0.002255261224945234
# HELP system_cpu_count The number of processors available to the Java virtual machine
# TYPE system_cpu_count gauge
system_cpu_count 4.0
# HELP tomcat_global_error_total
# TYPE tomcat_global_error_total counter
tomcat_global_error_total{name="http-nio-8080",} 0.0
# HELP tomcat_threads_busy
# TYPE tomcat_threads_busy gauge
tomcat_threads_busy{name="http-nio-8080",} 1.0
...
```

修改 Prometheus 工作目录里面的 `prometheus.yml` 配置文件，添加一个 `scrape_config`: 

```yaml
scrape_configs:
  - job_name: 'spring-restbucks'

    metrics_path: /actuator/prometheus

    static_configs:
    - targets: ['localhost:8080']
```

然后启动 Prometheus：

```shell
$ ./prometheus
level=info ts=2018-11-24T15:38:05.919729Z caller=main.go:244 msg="Starting Prometheus" version="(version=2.5.0, branch=HEAD, revision=67dc912ac8b24f94a1fc478f352d25179c94ab9b)"
level=info ts=2018-11-24T15:38:05.920045Z caller=main.go:245 build_context="(go=go1.11.1, user=root@578ab108d0b9, date=20181106-11:45:24)"
level=info ts=2018-11-24T15:38:05.920072Z caller=main.go:246 host_details=(darwin)
level=info ts=2018-11-24T15:38:05.920099Z caller=main.go:247 fd_limits="(soft=4864, hard=unlimited)"
level=info ts=2018-11-24T15:38:05.920125Z caller=main.go:248 vm_limits="(soft=unlimited, hard=unlimited)"
level=info ts=2018-11-24T15:38:05.921263Z caller=main.go:562 msg="Starting TSDB ..."
level=info ts=2018-11-24T15:38:05.922839Z caller=web.go:399 component=web msg="Start listening for connections" address=0.0.0.0:9090
...
level=info ts=2018-11-24T15:38:05.964013Z caller=main.go:572 msg="TSDB started"
level=info ts=2018-11-24T15:38:05.964418Z caller=main.go:632 msg="Loading configuration file" filename=prometheus.yml
level=info ts=2018-11-24T15:38:05.971224Z caller=main.go:658 msg="Completed loading of configuration file" filename=prometheus.yml
level=info ts=2018-11-24T15:38:05.971344Z caller=main.go:531 msg="Server is ready to receive web requests."
```

> Prometheus 是一个时间序列数据库，广泛使用在 Kubernetes 集群的监控系统中。关于这个组件有一个不错的介绍：
> https://www.ibm.com/developerworks/cn/cloud/library/cl-lo-prometheus-getting-started-and-practice/index.html
> 
> Prometheus 采用的存储结构非常高效，每个指标仅占用 3.5 字节。并且采用 Write Ahead Log 技术，定时刷盘，加快入库性能，确保宕机重启的情况下持久保存。这种数据结构和存储方式与 OpenTSDB + HBase 十分相似。可以认为 Prometheus 是 OpenTSDB + HBase 的单机版。如果已经运行了 HBase，使用 OpenTSDB 也是一个不错的选择。
> 
> Prometheus 定时拉数据，如果目标程序在两次拉取之间停止，总会丢失一些指标数据。我们收集数据的目的是了解程序的运行和使用情况，定位性能问题，所以丢失少量数据是可以接受的。如果采用实时存储方式处理指标数据，必然会影响业务本身的性能。当然我们可以使用消息队列来提高速度，但是又带来重复统计的问题，接着必须在存储操作上实现幂等性，这些操作和检查都会消耗额外的资源。定时存储是一个可以接受的权衡。

Prometheus 启动之后，很快就可以查看指标数据，我们查看一下 `sleep.count` 指标：

![sleep_count](https://github.com/lane-cn/spring-boot-2-metrics-sample/blob/master/images/sleep_count.png?raw=true)

还是老问题，计数器存储的是累加值，我们看到 `sleep.count` 不断的累加。程序重启重置到 0，然后再不断累加。我们想要看到的是每次采集的增量值，可以使用 `idelta` 函数：

```
idelta(sleep_count_total[1m])
```

统计结果是这样，正是我们想要的：

![sleep_count_delta](https://github.com/lane-cn/spring-boot-2-metrics-sample/blob/master/images/sleep_count_delta.png?raw=true)

`sleep.time` 是一个计时器，Micrometer 向 Prometheus 报告了计时次数和总时长，我们可以计算每次报告的响应时间：

```
idelta(sleep_time_seconds_sum[1m]) / idelta(sleep_time_seconds_count[1m])
```

结果是这样：

![sleep_time](https://github.com/lane-cn/spring-boot-2-metrics-sample/blob/master/images/sleep_time.png?raw=true)

> Prometheus 使用 PromQL 实现数据查询，内置不少函数，实现数据查询以及数据格式化。官网文档：
> https://prometheus.io/docs/prometheus/latest/querying/basics/

Prometheus 的图形界面也是比较阳春的，我们当然可以自己做，有现成的当然更好。Grafana 集成了 Prometheus 数据源。可以用 Grafana 做可视化界面。在 Grafana 定义 Prometheus 数据源：

![grafana_datasource](https://github.com/lane-cn/spring-boot-2-metrics-sample/blob/master/images/grafana_datasource.png?raw=true)

图形化界面：

![grafana_dashboard](https://github.com/lane-cn/spring-boot-2-metrics-sample/blob/master/images/grafana_dashboard.png?raw=true)



