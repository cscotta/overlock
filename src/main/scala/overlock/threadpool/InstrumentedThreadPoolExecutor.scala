//
// Copyright 2011, Boundary
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package overlock.threadpool

import java.util.concurrent._
import com.yammer.metrics._
import core._

class InstrumentedThreadPoolExecutor(path : String,
    name : String, 
    corePoolSize : Int,
    maximumPoolSize : Int,
    keepAliveTime : Long,
    unit : TimeUnit,
    workQueue : BlockingQueue[Runnable],
    factory : ThreadFactory,
    handler : RejectedExecutionHandler) extends 
    ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,unit,workQueue,factory,handler) with 
    Instrumented {
  override protected lazy val metricsGroup = new MetricsGroup(NameBuilder(path, name))
  val requestRate = metrics.meter("request", "requests", TimeUnit.SECONDS)
  val rejectedRate = metrics.meter("rejected", "requests", TimeUnit.SECONDS)
  val executionTimer = metrics.timer("execution")
  val queueGauge = metrics.gauge("queue size")(getQueue.size)
  val threadGauge = metrics.gauge("threads")(getPoolSize)
  val activeThreadGauge = metrics.gauge("active threads")(getActiveCount)
  val startTime = new ThreadLocal[Long]
  
  setRejectedExecutionHandler(new RejectedExecutionHandler {
    def rejectedExecution(r : Runnable, executor : ThreadPoolExecutor) {
      rejectedRate.mark
      handler.rejectedExecution(r,executor)
    }
  })
  
  override def execute(r : Runnable) {
    requestRate.mark
    super.execute(r)
  }
  
  override protected def beforeExecute(t : Thread, r : Runnable) {
    startTime.set(System.nanoTime)
  }
  
  override protected def afterExecute(r : Runnable, t : Throwable) {
    val duration = System.nanoTime - startTime.get
    executionTimer.update(duration, TimeUnit.NANOSECONDS)
  }
}