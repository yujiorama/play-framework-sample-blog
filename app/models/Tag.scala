package models

import play.db.jpa.{Entity,Model}

@Entity
class Tag(
  var name: String
) extends Model with Ordered[Tag] {
  def this() = this("")
  
  override def toString() = name
  
  def compare(other: Tag) = name.compareTo(other.name)
}
