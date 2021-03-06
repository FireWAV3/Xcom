package XCOM.aView.gui

import java.awt.Toolkit

import XCOM.controller.controllerComponent._
import XCOM.model.PlayerStatus._
import XCOM.util.UndoManager
import javax.swing.{Icon, ImageIcon}

import scala.collection.mutable.ListBuffer
import scala.swing.Swing.{EmptyIcon, LineBorder}
import scala.swing.event.{Event, MouseClicked}
import scala.swing.{BorderPanel, BoxPanel, Dimension, FlowPanel, Frame, GridPanel, Label, MainFrame}
import scala.util.Try
//Class representing the GUI while the field is already generated
class GameField(c: ControllerInterface, uManager: UndoManager) extends Frame{




  def main = new MainFrame{
    title = "XCOM"
    listenTo(c)
    val localFile = System.getProperty("user.dir")

    var defaultSize = Toolkit.getDefaultToolkit.getScreenSize.height - 180

    //Bottom row of the GUI
    var info = new GridPanel(1,2){
      background = java.awt.Color.BLUE.darker().darker().darker()

      minimumSize = new Dimension(1000, 150)
      preferredSize = new Dimension(1000, 150)
      var infoLabel =  new Label("Info"){
      }

      //Buttons of Bottom row
      var next = new GridPanel(1,3){
         background = java.awt.Color.BLUE.darker().darker().darker()

        //Help-Button
         contents += new Label(){
           text = "<html> <img src=\"file:///"+ localFile +"/src/main/scala/XCOM/aView/gui/img/help_button.png\" width = 100 height= 50> </html>"
           listenTo(mouse.clicks)
           reactions += {
             case MouseClicked(scr,pt,mod,clicks,pops) => c.help
           }
         }

        //Next-Button
         contents += new Label(){
             text = "<html> <img src=\"file:///"+ localFile +"/src/main/scala/XCOM/aView/gui/img/next_button.png\" width = 100 height= 50> </html>"
             listenTo(mouse.clicks)
             reactions += {
               case MouseClicked(scr,pt,mod,clicks,pops) => uManager.doStep(c); c.next()
             }
         }

        //Exit-Button
         contents += new Label(){
           text = "<html> <img src=\"file:///"+ localFile +"/src/main/scala/XCOM/aView/gui/img/exit_button.png\" width = 100 height= 50> </html>"

           listenTo(mouse.clicks)
           reactions +={
             case MouseClicked(scr,pt,mod,clicks,pops) => {
               Try(c.exit)
               System.exit(0)
             }
           }
         }
      }

      contents += infoLabel
      contents += next
    }

    //Chat fields on the right of the GUI
    var chat = new GridPanel(2,1){
      background = java.awt.Color.BLUE.darker().darker().darker()

      //Red Chat
      var chatREDLabel =  new Label("CHAT RED"){
        text = "<html><div style=\"background-color:be0000; padding: 10px;color:black\">RED Chat</div> <div style=\"padding: 10px; background-color:557786; border-width: 7px; padding-right:20px ;color:#ffd900;  border-style:solid ;border-color:#be0000;\">" +"      " + "</div> </html>"

        var messages = ListBuffer[String]()
        minimumSize = new Dimension(200, 380)
        preferredSize = new Dimension(200, 380)
      }

      //Blue Chat
      var chatBLUELabel =  new Label("CHAT BLUE"){
        text = "<html><div style=\"background-color:be0000; padding: 10px;color:black\">RED Chat</div> <div style=\"padding: 10px; background-color:557786; border-width: 7px; padding-right:20px ;color:#ffd900;  border-style:solid ;border-color:#be0000;\">" + "      " + "</div> </html>"

        var messages = ListBuffer[String]()
        minimumSize = new Dimension(200, 380)
        preferredSize = new Dimension(200, 380)
      }
      contents += chatREDLabel
      contents += chatBLUELabel
    }

    var cInput = ""
    var highlightedCell = Array[IdLabel](new IdLabel(""), new IdLabel(""))

    //Gamefield shown in GUI
    var field = new GridPanel(c.field.sizeY+1,c.field.sizeX+1){
      background = java.awt.Color.decode("#ffc400")
      //size of Field
      val prefUnit = defaultSize / c.field.sizeY
      val newX = prefUnit * c.field.sizeX
      val newY = prefUnit * c.field.sizeY
      minimumSize = new Dimension(newX, newY)
      preferredSize = new Dimension(newX, newY)
      maximumSize = new Dimension(newX, newY)

      //Tiles of Field
      val cells = Array.ofDim[IdLabel](c.field.sizeX+1,c.field.sizeY+1)
      for( y <- 1 to c.field.sizeY+1){
        for( x <- 1 to c.field.sizeX+1){

          //Defining a Single Tile
          val heroLabel = new IdLabel(""){

            val tempSize = size.height
            minimumSize = new Dimension(tempSize, tempSize)
            preferredSize = new Dimension(tempSize, tempSize)
            maximumSize = new Dimension(tempSize, tempSize)

            recolor(this)
            listenTo(mouse.clicks)
            size.width = size.height

            reactions +={
              case MouseClicked(scr,pt,mod,clicks,pops) => {
                mod match {
                  case 0 => { //left Mousebutton
                    if(id.contains("C")){
                      highlightedCell(0) = highlightedCell(1)
                      highlightedCell(1) = this
                      c.info(Some(id))
                    }
                  }
                  case 256 => { //right Mousebutton
                    val cell = highlightedCell(1)
                    val place = findCell(this)
                    uManager.doStep(c)
                    if(!c.move(cell.id,place._1+1,place._2+1)) uManager.undoClear(c)
                  }
                  case _ => { //middle Mousebutton
                    val cell = highlightedCell(1)
                    c.aim(Some(cell.id),Some(this.id))
                  }
                }
              }
            }
          }
          cells(x-1)(y-1) = heroLabel
          contents += heroLabel
        }
      }
    }

    //Highlight the selected Character and show his stats
    def infoupdate(): Unit = {
      val output = "<html> <div style=\"padding: 10px; background-color:557786; padding-right:20px ;color:#ffd900;  border-style:solid ;border-color:#ffd900;\">" + c.output.replaceAll("\n","<br/>") + "</div> </html>"
      info.infoLabel.text = output
      recolor(highlightedCell(0))
      highlightedCell(1).border = LineBorder(java.awt.Color.GREEN, 3)
      repaint()
    }

    //Update the Chat-Messages
    def chatupdate(): Unit = {
      c.PlayerState match {
        case BLUE => {
          if(this.chat.chatBLUELabel.messages.size >= 5){ //Maximum of 5 Messages per chat
            this.chat.chatBLUELabel.messages = rotateArray(this.chat.chatBLUELabel.messages,c.output)
          } else {
            this.chat.chatBLUELabel.messages += c.output
          }
          var chatMessage = ""  //"CHAT BLUE\n\n"
          for (x <- this.chat.chatBLUELabel.messages) {
            chatMessage = chatMessage + x
            chatMessage += "\n"
          }
          this.chat.chatBLUELabel.text = "<html> <div style=\"background-color:0014db; padding: 10px; color:black\">BLUE Chat</div> <div style=\"padding: 10px; background-color:557786; border-width: 7px; padding-right:20px ;color:#ffd900;  border-style:solid ;border-color:#0014db;\">" + chatMessage.replaceAll("\n","<br/>") + "</div> </html>"
          repaint()
        }
        case RED => {
          if(this.chat.chatREDLabel.messages.size >= 5){ //Maximum of 5 Messages per Chat
            this.chat.chatREDLabel.messages = rotateArray(this.chat.chatREDLabel.messages,c.output)
          } else {
            this.chat.chatREDLabel.messages += c.output
          }
          var chatMessage =  "" //"CHAT RED\n\n"
          for (x <- this.chat.chatREDLabel.messages) {
            chatMessage += x
            chatMessage += "\n"
          }
          this.chat.chatREDLabel.text = "<html><div style=\"background-color:be0000; padding: 10px;color:black\">RED Chat</div> <div style=\"padding: 10px; background-color:557786; border-width: 7px; padding-right:20px ;color:#ffd900;  border-style:solid ;border-color:#be0000;\">" + chatMessage.replaceAll("\n","<br/>") + "</div> </html>"
          repaint()
        }
      }
    }

    //Ask for user permission to shoot
    def shootupdate() = {
        new DecisionPanel(c,c.output, uManager)
    }

    //repaint the whole field
    def fieldupdate() = {
      for(a <- 0 to field.cells.length-1){
        for (b <- 0 to field.cells(a).length-1){
          field.cells(a)(b).id = findType(a,b)
          recolor(field.cells(a)(b))
        }
      }
      highlightedCell(0) = new IdLabel("")
      highlightedCell(1) = new IdLabel("")
      info.infoLabel.text = ""
      repaint()
      chatupdate()
    }

    //used to swap Chat-Messages
    def rotateArray(array: ListBuffer[String], string: String): ListBuffer[String] = {
      var newArray = ListBuffer[String]()
      for (x <- 1 to array.size-1){
        newArray += array(x)
      }
      newArray += string
      newArray
    }

    //get Position of Cell by searching with the Name
    def findCell(cell: IdLabel): (Int, Int) = {
      for (x <- 0 to field.cells.size){
        val y = field.cells(x).indexOf(cell)
        if (y != -1) return (x,y)
      }
      (-1,-1)
    }

    //get Type(Character,Rock,Empty) of a Cell
    def findType(x: Int, y: Int): String ={
      for (a <- c.getCharacters) {
        if (a._2 == x && a._3 == y) {
          return a._1
        }
      }
      for (a <- c.getRocks) {
        if (a._1 == x && a._2 == y) {
          return "R"
        }
      }
      "X"
    }

    //redefine the look of a Cell given her type
    def recolor(cell: IdLabel): Unit ={
      cell.id match {
        case "X" => {//default Cell
          cell.icon = EmptyIcon
          cell.text = ""
          cell.background = java.awt.Color.YELLOW
          cell.text = "<html> <div style=\"background-color:ffc400; color:ffc400; padding: 1000px; width=100%; height=100%\"> placeHoler</div> </html>"
          cell.icon = new ImageIcon("src/main/scala/XCOM/aView/gui/img/back.png")
          cell.border = LineBorder(java.awt.Color.BLACK, 1)
        }
        case "R" => {//Rock
          cell.icon = new ImageIcon("src/main/scala/XCOM/aView/gui/img/rock.png")
          cell.background = java.awt.Color.GRAY
          cell.border = LineBorder(java.awt.Color.BLACK, 1)
        }
        case _ => {//Character

          //cell.text = cell.id
         c.getCharactersSide(cell.id) match {
           case 0 => {//Team Blue
             cell.border = LineBorder(java.awt.Color.BLUE, 3)
             cell.background = java.awt.Color.YELLOW
             cell.icon = new ImageIcon("")
             cell.text = "<html> <div style=\"background-color:ffc400; color:ffc400;  \"> <img src=\"file:///"+ localFile +"/src/main/scala/XCOM/aView/gui/img/"+c.getCharactersTypeIcon(cell.id)+"_blue.png\" width = "+ (cell.size.height- cell.size.height/6) +" height= "+ (cell.size.height- cell.size.height/6) +"></div>" +
               " </html>"
           }
           case 1 => {//Team Red
             cell.border = LineBorder(java.awt.Color.RED, 3)
             cell.background = java.awt.Color.YELLOW
             cell.icon = new ImageIcon("")
             cell.text = "<html> <div style=\"background-color:ffc400; color:ffc400;   \"> <img src=\"file:///"+ localFile +"/src/main/scala/XCOM/aView/gui/img/"+c.getCharactersTypeIcon(cell.id)+"_red.png\" width = "+ (cell.size.height- cell.size.height/6) +" height= "+ (cell.size.height- cell.size.height/6) +"></div>" +
               " </html>"
           }
           case _ => cell.border = LineBorder(java.awt.Color.YELLOW, 3)
         }
        }
      }
      cell.size.width = cell.size.height
    }

    reactions += {
      case event: UpdateInfo => infoupdate()
      case event: UpdateText => chatupdate()
      case event: UpdateField => fieldupdate()
      case event: UpdateShoot => shootupdate()
      case event: UpdateHelp => {new HelpFrame(c); println("call")}
    }

    val outerField = new FlowPanel{
      contents += field
      background = java.awt.Color.BLUE.darker().darker()
    }
    background = java.awt.Color.BLUE.darker().darker().darker()
    contents = new BorderPanel{
      background = java.awt.Color.BLUE.darker().darker().darker()
      add(outerField,BorderPanel.Position.Center)
      add(info,BorderPanel.Position.South)
      add(chat,BorderPanel.Position.East)
    }

    maximize()
    resizable = false

  }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//Type of Labels for Cells
class IdLabel(var id: String, string0: String, icon0: Icon) extends Label{
  text = string0
  icon = icon0
  def this(id: String){
    this(id,id,EmptyIcon)
  }

  override def toString(): String = "id: " + id + "\ttext: " + text
}

//Frame to show the Helptext
class HelpFrame(c:ControllerInterface) extends MainFrame {
  listenTo(c)
  println("Window")
  background = java.awt.Color.BLUE.darker().darker().darker()
  title = "Need some advice Commander?"

  val helpMessage = ("Use Left Mousebutton to select a Character\nUse Right Mousebutton to move the selected Character"
    + "\n    to the tile you clicked on\nUse Middle Mousebutton to shoot with your selected Character"
    + "\n    on the Character you clicked on\nTips:\nCheck Info at the Bottom to learn everything about the Characters"
    + "\n Click on 'Next' to skip parts of your turn\nHold your clicks pressed a little longer because Scala Swing is"
    + "\n    a bit laggy sometimes")
  contents = new FlowPanel(){
    background = java.awt.Color.BLUE.darker().darker().darker()
    contents += new Label("<html> <div style=\"padding: 10px; background-color:557786; padding-right:20px ;color:#ffd900;  border-style:solid ;border-color:#ffd900;\">" + helpMessage.replaceAll("\n","<br/>") + "</div> </html>"){
      background = java.awt.Color.BLUE.darker().darker().darker()
    }
  }

  override def closeOperation(): Unit = dispose()

  size = new Dimension(500,250)
  resizable = false
  visible = true
  centerOnScreen()
}

//Frame to ask a player if he wants to shoot
class DecisionPanel(c:ControllerInterface,output:String, uManager: UndoManager) extends MainFrame {
  listenTo(c)
  val localFile = System.getProperty("user.dir")
  background = java.awt.Color.BLUE.darker().darker().darker()
  title = "Affirmation needed"
  val question = new GridPanel(1,2){
    background = java.awt.Color.BLUE.darker().darker().darker()
    var yes = new Label(){
      background = java.awt.Color.BLUE.darker().darker().darker()
      listenTo(mouse.clicks)
      text = "<html> <img src=\"file:///"+ localFile +"/src/main/scala/XCOM/aView/gui/img/yes_button.png\"  width = 80 height= 50 ></html>"
      reactions += {
        case MouseClicked(scr,pt,mod,clicks,pops) =>
          uManager.doStep(c)
          Try(c.shoot(true))

      }
    }
    var no = new Label(){
      background = java.awt.Color.BLUE.darker().darker().darker()
      text = "<html> <img src=\"file:///"+ localFile +"/src/main/scala/XCOM/aView/gui/img/no_button.png\" width = 80 height= 50></html>"
      listenTo(mouse.clicks)
      reactions += {
        case MouseClicked(scr,pt,mod,clicks,pops) =>  c.shoot(false)
          dispose()
      }
    }
    contents += yes
    contents += no
  }

  contents = new GridPanel(2,1){
    background = java.awt.Color.BLUE.darker().darker().darker()
    contents += new Label("<html> <div style=\"padding: 10px; background-color:557786; padding-right:20px ;color:#ffd900;  border-style:solid ;border-color:#ffd900;\">" + output.replaceAll("\n","<br/>") + "</div> </html>")
    contents += question
  }
  size = new Dimension(400,150)
  resizable = false
  visible = true
  centerOnScreen()
  override def closeOperation(): Unit = {c.shoot(false);dispose()}

  reactions += {
    case event: UpdateField => dispose()
    case event: UpdateText => dispose()
  }

}