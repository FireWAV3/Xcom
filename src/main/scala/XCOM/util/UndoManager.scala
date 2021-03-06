package XCOM.util

import XCOM.controller.controllerComponent._

//Used to undo and redo moves
case class UndoManager() {
  private var undoStack : List[ControllerInterface]= Nil
  private var redoStack : List[ControllerInterface]= Nil

  def doStep(c: ControllerInterface) ={//save the current state
    val newC = c.deepCopy()
    redoStack = Nil
    undoStack = newC::undoStack
  }

  def undoStep(c: ControllerInterface) : ControllerInterface ={//load the previous state
    undoStack match {
      case  Nil => c
      case head::stack => {
        val oldhead = head
        val redoHead = c.deepCopy()
        undoStack = stack
        redoStack = redoHead::redoStack
        oldhead
      }
    }
  }

  def undoClear(c: ControllerInterface) : ControllerInterface ={//used to remove a failing state
    undoStack match {
      case  Nil => c
      case head::stack => {
        val oldhead = head
        undoStack = stack
        oldhead
      }
    }
  }

  def redoStep(c: ControllerInterface) : ControllerInterface ={//used to reload the previous state on which the user called undo
    redoStack match {
      case Nil => c
      case head::stack =>{
        val oldhead =  head
        val undoHead = c.deepCopy()
        redoStack = stack
        undoStack = undoHead::undoStack
        oldhead
      }
    }
  }
}
