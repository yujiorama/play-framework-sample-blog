package models

import java.util.{Date,List,ArrayList,Set,TreeSet}

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
  
  @ManyToMany(cascade=Array(CascadeType.PERSIST))
  var tags: Set[Tag] = new TreeSet[Tag]

  def tagItWith(name: String): Post = {
    tags.add(Tag.findOrCreateByName(name))
    this
  }
  
  def next(): Post = {
    Post.find("postedAt < ? order by postedAt desc", this.postedAt).first match {
      case Some(ok: Post) => ok
      case None => null
    }
  }
  
  def previous(): Post = {
    Post.find("postedAt > ? order by postedAt asc", this.postedAt).first match {
      case Some(ok: Post) => ok
      case None => null
    }
  }
  
  def this() = this(null, null, null)
}

object Post extends QueryOn[Post] {
  def findTaggedWith(name: String): List[Post] = {
    new ArrayList[Post]()
  }
}
