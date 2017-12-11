import sangria.execution.deferred.HasId


object Models {
  type ChannelId = Int
  /**
   * @author wild
   */
  trait Identifiable {
    def id: Int
  }
  case class Channel(id:ChannelId, name:String) extends Identifiable
  
  object Channel{
    implicit val hasId=HasId[Channel, ChannelId](_.id)
  }
}