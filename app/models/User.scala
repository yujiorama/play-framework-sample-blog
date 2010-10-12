package models

import java.util._

import play.db.jpa._

@Entity
class User(
  var email: String,
  var password: String,
  var fullname: String
) extends Model {
  var isAdmin: Boolean = false
  
  def this() = this("", "", "")
}


object User extends QueryOn[User] {
  def connect(email: String, password: String): Option[User] = {
    return this.find("byEmailAndPassword", email, password).first
  }
}
