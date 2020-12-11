package XCOM.controller
import XCOM.model

trait TravelStrategyTrait{
  def movePossible(hero:model.Character, pX:Int, pY:Int):Boolean
}

class ContextTravel(c:Controller){
  def deepCoppy(): ContextTravel = {
    var CTout = new ContextTravel(this.c)
    CTout.travelState = this.travelState
    CTout
  }

  var travelState:TravelStrategyTrait = new Manhattan(c)
}

class TravelStrategy (c:Controller) extends TravelStrategyTrait {
  override def movePossible(hero: model.Character, pX: Int, pY: Int): Boolean = throw new Exception("wtf how?")
}

class AStar(c:Controller)extends TravelStrategy(c){
  override def movePossible(hero: model.Character, pX: Int, pY: Int): Boolean = {
    c.aStarMove(hero.cell.x,hero.cell.y,pX,pY)
  }
}

class Manhattan(c:Controller)extends TravelStrategy(c){
  override def movePossible(hero: model.Character, pX: Int, pY: Int): Boolean = {
    val xDistance = Math.abs(pX - 1 - hero.cell.x)
    val yDistance = Math.abs(pY - 1 - hero.cell.y)
    val distance =  xDistance + yDistance
    if(hero.mrange >= distance) true else throw new Exception("Move not possible: Hero can't move this far")
  }
}

