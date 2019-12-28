package zio.aws.lambda

package object environment {
  def handler: ZIO[Environment, Error, String] = ZIO.accessM(_.environment.handler)
  def region: ZIO[Environment, Error, Region] = ZIO.accessM(_.environment.region)
  def runtimeApi: ZIO[Environment, Error, String] = ZIO.accessM(_.environment.runtimeApi)
  def runtimeDir: ZIO[Environment, Error, String] = ZIO.accessM(_.environment.runtimeDir)
  def taskRoot: ZIO[Environment, Error, String] = ZIO.accessM(_.environment.taskRoot)
  def functionName: ZIO[Environment, Error, String] = ZIO.accessM(_.environment.functionName)
  def functionMemorySize: ZIO[Environment, Error, String] = ZIO.accessM(_.environment.functionMemorySize)
  def functionVersion: ZIO[Environment, Error, String] = ZIO.accessM(_.environment.functionVersion)
  def logGroupName: ZIO[Environment, Error, String] = ZIO.accessM(_.environment.logGroupName)
  def logStreamName: ZIO[Environment, Error, String] = ZIO.accessM(_.environment.logStreamName)
  def awsExecutionEnv: ZIO[Environment, Error, String] = ZIO.accessM(_.environment.awsExecutionEnv)
  def awsAccessKeyId: ZIO[Environment, Error, String] = ZIO.accessM(_.environment.awsAccessKeyId)
  def awsSecretAccessKey: ZIO[Environment, Error, String] = ZIO.accessM(_.environment.awsSecretAccessKey)
  def awsSessionToken: ZIO[Environment, Error, String] = ZIO.accessM(_.environment.awsSessionToken)
  def lang: ZIO[Environment, Error, String] = ZIO.accessM(_.environment.lang)
  def timeZone: ZIO[Environment, Error, String] = ZIO.accessM(_.environment.timeZone)
  def path: ZIO[Environment, Error, String] = ZIO.accessM(_.environment.path)
  def setTraceId(tid: String): ZIO[Environment, Error, Unit] = ZIO.accessM(_.environment.setTraceId(tid))
}
