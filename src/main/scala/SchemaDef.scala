import sangria.schema._
import sangria.execution.deferred.{DeferredResolver, Fetcher, Relation, RelationIds}


object SchemaDef {
  import Models._
  import sangria.macros.derive._
  
  val ChannelType = ObjectType[Unit,Channel](
    "Channel",
    "Chat Channel",
    
    fields[Unit,Channel](
      Field("id", IntType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name)
    )
  )
  
  val channelFetcher = Fetcher(
    (repo: ChatRepository, ids: Seq[ChannelId]) => repo.channels(ids)
  )
  
  val deferredResolver = DeferredResolver.fetchers(channelFetcher)
  
  val IdArg = Argument("id", IntType)
  val NameArg = Argument("name", StringType)
  
  def constantComplexity[Ctx](complexity: Double) =
    Some((_: Ctx, _: Args, child: Double) ⇒ child + complexity)

  val QueryType = ObjectType(
    "Query",
    fields[ChatRepository, Unit](
      Field("allChannels", ListType(ChannelType),
        description = Some("Returns a list of all available channels."),
        complexity = constantComplexity(100),
        resolve = _.ctx.allChannels
      ),
      Field("channels",ListType(ChannelType),
         description = Some("Returns a list of channels for provided IDs."),
        arguments = Argument("ids", ListInputType(IntType)) :: Nil,
        resolve = c => channelFetcher.deferSeqOpt(c.arg[List[ChannelId]]("ids"))
      ),
      Field("channel", OptionType(ChannelType),
        description = Some("Returns a channel with specific `id`."),
        arguments = Argument("id", IntType) :: Nil,
        resolve = c => channelFetcher.defer(c.arg[ChannelId]("id"))),
    )
  )

  val ChatSchema = Schema(QueryType) 
 
}