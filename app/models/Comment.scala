package models

import java.util.Date

import play.db.jpa._

@Entity
class Comment(
  @ManyToOne var post: Post,
  var author: String,
  @Lob var content: String
) extends Model {
  
  var postedAt: Date = new Date()

  def this() = this(null, null, null)
}

object Comment extends QueryOn[Comment]
