package POJOs

  sealed trait RelationshipStatus
  case class Single() extends RelationshipStatus
  case class In_A_Relationship() extends RelationshipStatus
  case class Its_Complicated() extends RelationshipStatus
