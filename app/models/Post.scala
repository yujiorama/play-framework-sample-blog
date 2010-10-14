package models

import java.util.ArrayList
import java.util.Date
import java.util.List

import play.db.jpa._

@Entity
class Post(
  @ManyToOne var author: User,
  var title: String,
  @Lob var content: String
) extends Model {

  var postedAt: Date = new Date()

  @OneToMany(mappedBy="post", cascade=Array(CascadeType.ALL))
  var comments: List[Comment] = new ArrayList[Comment]
  
  def addComment(author: String, content: String) = {
    val newComment = new Comment(this, author, content).save()
    comments.add(newComment)
  }
  
  def this() = this(null, null, null)
}

object Post extends QueryOn[Post]