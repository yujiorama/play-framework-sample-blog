package models

import play.db.jpa.{Entity,Model}

@Entity
class Tag(
  var name: String
) extends Model with Ordered[Tag] {
  
  override def toString() = name
  
  def compare(other: Tag) = name.compareTo(other.name)

  def this() = this("")
}


object Tag extends QueryOn[Tag] {
  def findOrCreateByName(name: String): Tag {
    new Tag(name)
  }
}
