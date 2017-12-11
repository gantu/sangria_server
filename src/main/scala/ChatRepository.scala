
import slick.jdbc.H2Profile.api._
import Models._
import scala.concurrent.{Await,Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class ChatRepository(db: Database) {
    import ChatRepository._
    
    def allChannels = db.run(Channels.result)
   
    def channel(id:ChannelId):Future[Option[Channel]] = {
      db.run(Channels.filter(_.id === id).result.headOption)
    }
    
    def channels(ids: Seq[ChannelId]): Future[Seq[Channel]] = db.run(Channels.filter(_.id inSet ids).result)
}

object ChatRepository{
  class ChannelTable(tag: Tag) extends Table[Channel](tag, "Channels"){
    def id = column[ChannelId]("CHANNEL_ID",O.PrimaryKey)
    def name = column[String]("NAME")
    def * = (id, name) <> ((Channel.apply _).tupled, Channel.unapply)
  }
  val Channels = TableQuery[ChannelTable]
  
  val databaseSetup = DBIO.seq(
      (Channels.schema).create,
      Channels ++= Seq(
        Channel(1,"Auto"),
        Channel(2,"Technology"),
        Channel(3,"Entertainment"),
        Channel(4,"Books"),
        Channel(5,"Zoo")
    )    
  )
  
  def createDatabase() = {
    val db = Database.forConfig("h2mem")
    Await.result(db.run(databaseSetup), 10 seconds)
    new ChatRepository(db)
  }
}