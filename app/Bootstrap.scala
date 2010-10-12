import play.jobs._
import play.test.Fixtures

import models.User

@OnApplicationStart
class Bootstrap extends Job {
  
  override def doJob() {
    if (0 == User.count()) {
      Fixtures.load("initial-data.yml")
    }
  }

}
