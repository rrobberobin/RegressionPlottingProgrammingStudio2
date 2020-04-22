package regression

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene._
import scalafx.scene.chart.ScatterChart
import scalafx.collections.ObservableBuffer
import scalafx.scene.chart.NumberAxis
import scalafx.scene.chart.XYChart
import scalafx.scene.chart.LineChart
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.event.ActionEvent
import scalafx.Includes._
import scalafx.stage.FileChooser
import scalafx.geometry.Orientation

object Plotting extends JFXApp {

  var inputFile = new FileInput("Statfin.csv") //If It's empty, throw an error

  var dataPerLine = inputFile.pairs.size / 5 //More dataPerLine means less lines //cant be too many lines or can it?

  //creates the axes for the plots
  var xAxis = NumberAxis()
  var yAxis = NumberAxis()

  var xAxis2 = NumberAxis()
  var yAxis2 = NumberAxis()
  var xAxis3 = NumberAxis()
  var yAxis3 = NumberAxis()

  if(inputFile.label.size>1){
  xAxis.label = inputFile.label(0)
  yAxis.label = inputFile.label(1)
  xAxis2.label=inputFile.label(0)
  yAxis2.label=inputFile.label(1)
  xAxis3.label=inputFile.label(0)
  yAxis3.label=inputFile.label(1)
  }
  
  def updateAll { //If It's empty, throw an error
    
    //the data array
    val pairs = inputFile.pairs
    
    // the slope of the Simple Regression line
    val lineSlope = RegressionMath.slope(pairs.map(_._1), pairs.map(_._2))
    
    //the y intersect of the same line
    val xIsZeroAt = RegressionMath.yIntersect(pairs.map(_._1), pairs.map(_._2))
    
    //The same line in array form. 
    //The form of the line is: Array((starting x-coordinate, starting y-coordinate)(ending x-coordinate, ending y-coordinate))
    //Mathematically the line would be infinitely long, but we are restriciting the size according to the used data
    val line = Array((pairs.head._1, xIsZeroAt), (pairs.last._1, xIsZeroAt + lineSlope * pairs.last._1))
    
    // the Segmented Regression lines. They are grouped together in an array where each positon in the array represent one line.
    val grouped = pairs.sliding(dataPerLine, dataPerLine - 1).toList // Shouldn't work if dataPerLine is 1 or less
    
    //the x-coordinate of the first and the last datapoint
    val allX = grouped.map(g => (g.head._1, g.last._1))

    // the slope of the Segmented Regression lines
    val groupedSlope = grouped.map(g => RegressionMath.slope(g.map(_._1), g.map(_._2)))
    
    //the y intersect of the same lines
    val groupedZeroAt = grouped.map(g => RegressionMath.yIntersect(g.map(_._1), g.map(_._2)))
    
    //the y intersect, slopes and x-coordinates geouped together. This is done to easily trasnform the lines into array form
    val together = (groupedZeroAt zip groupedSlope) zip allX

    //the lines in array form
    val groupedLines = together.map(g => Array((g._2._1, g._1._1 + g._1._2 * g._2._1), (g._2._2, g._1._1 + g._1._2 * g._2._2)))

    stage = new JFXApp.PrimaryStage {
      title = "Total"
      scene = new Scene(1100, 500) {

        val data = XYChart.Series[Number, Number]("Scatter", ObservableBuffer(pairs.map(each => XYChart.Data[Number, Number](each._1, each._2)): _*))
        val xy = new ScatterChart(xAxis, yAxis, ObservableBuffer(data))

        val data2 = XYChart.Series[Number, Number]("Simple Regression", ObservableBuffer(line.map(each => XYChart.Data[Number, Number](each._1, each._2)): _*))
        val xy2 = new LineChart(xAxis2, yAxis2, ObservableBuffer(data2))

        val data3 = groupedLines.map(g => XYChart.Series[Number, Number]("Segmented Regression", ObservableBuffer(g.map(each => XYChart.Data[Number, Number](each._1, each._2)): _*)))
        val xy3 = new LineChart(xAxis3, yAxis3, ObservableBuffer(data3))

        val menuBar = new MenuBar
        val fileMenu = new Menu("File")

        val label = new Label("File: " + inputFile.file)
        //        label.layoutX = 50
        //        label.layoutY = 50

        val open = new MenuItem("Open")
        val exit = new MenuItem("Exit")

        val regressionMenu = new Menu("Regression")
        //val changeAxises = new MenuItem("Change Axises")

        val segmentsText = new TextField
        //        segmentsText.layoutX_=(1000)
        //        segmentsText.layoutY_=(1000)
        segmentsText.prefWidth = 350
        segmentsText.promptText = "How many datapoints per line. Hit enter to confirm"

        
        val changeLabels = new MenuItem("Change Labels")
        
//        changeLabels.onAction = (event:ActionEvent) => {
//          
//          selectedAxis = 
//          
//          val Axislabel = new TextField
//          Axislabel.prefWidth = 550
//          Axislabel.promptText = "Change labels of x and y axis. Write in the form: x-axis,y-axis  Hit enter to confirm"
//
//          menuPane.children = Iterable(menuBar, label, segmentsText, Axislabel)
//          
//          
//          Axislabel.onAction = (event: ActionEvent) => {
//            val text = Axislabel.text.apply.split(",")
//            if (2 < text.size) {
//              xAxis.label =text(0)
//              yAxis.label= text(1)
//              updateAll
//            }
//          }        
//          
//        }
        
        
        
        val changeDelimiter = new MenuItem("Change Delimiter")
        
        
        val changeAxisSize = new Menu("Change Axis")

        val ScatterX = new MenuItem("ScatterX")
        val ScatterY = new MenuItem("ScatterY")
        val SimpleX = new MenuItem("SimpleX")
        val SimpleY = new MenuItem("SimpleY")
        val SegmentX = new MenuItem("SegmentX")
        val SegmentY = new MenuItem("SegmentY")

        changeAxisSize.items = List(ScatterX, ScatterY, SimpleX, SimpleY, SegmentX, SegmentY)

        //val currentFile = new MenuItem("Current File")

        fileMenu.items = List(open, exit)
        regressionMenu.items = List(changeAxisSize,changeLabels)

        menuBar.menus = List(fileMenu, regressionMenu)

        val plotPane = new FlowPane()
        plotPane.children = Iterable(xy2, xy3)
        plotPane.setOrientation(Orientation.Vertical)

        val plotPane2 = new FlowPane()
        plotPane2.children = Iterable(xy)

        val rootPane = new FlowPane();
        //        val scene = new Scene(rootPane)

        val menuPane = new FlowPane()
        menuPane.children = Iterable(menuBar, label, segmentsText)

        rootPane.getChildren().addAll(menuPane, plotPane2, plotPane)

        root = rootPane

        //        content = List(menuBar, xy, xy2, xy3, label, segmentsText)

        exit.onAction = (event: ActionEvent) => sys.exit(0)

        open.onAction = (event: ActionEvent) => {

          val fileChooser = new FileChooser
          val selectedFile = fileChooser.showOpenDialog(stage)
          if (selectedFile != null) {
            val path = selectedFile.getPath
            if (path.endsWith(".csv") || path.endsWith(".tsv")) { //something other than csv here
              val oldfile = inputFile
              inputFile = new FileInput(path) // val path = selectedFile.getPath//.replaceAll("\\\\", "\\\\\\\\")
              label.text = "File: " + selectedFile
              if (!inputFile.pairs.isEmpty) {
                dataPerLine = inputFile.pairs.size / 5
                updateAll
              } else {
                inputFile = oldfile
                label.text = "The file you opened contains data in the wrong format"
              }
            } else label.text = "Wrong filetype: '" + path.substring(path.lastIndexOf(".") + 1) + "'. Should be csv or tsv"
          }
        }

        ScatterX.onAction = (event: ActionEvent) => {
          val AxisSize = new TextField
          AxisSize.prefWidth = 550
          AxisSize.promptText = "Change size of X-Axis for Chatter Plot. Write in the form:  UpperBound, lowerBound, tickSize   Hit enter to confirm"

          menuPane.children = Iterable(menuBar, label, segmentsText, AxisSize)

          AxisSize.onAction = (event: ActionEvent) => {
            val text = AxisSize.text.apply.split(",")
            if (2 < text.size) {
              val res = text.map(_.toInt)
              xAxis = new NumberAxis(res(0), res(1), res(2))
              updateAll
            }
          }
        }

        ScatterY.onAction = (event: ActionEvent) => {
          val AxisSize = new TextField
          AxisSize.prefWidth = 550
          AxisSize.promptText = "Change size of Y-Axis for ChatterPlot. Write in the form:  UpperBound, lowerBound, tickSize   Hit enter to confirm"

          menuPane.children = Iterable(menuBar, label, segmentsText, AxisSize)

          AxisSize.onAction = (event: ActionEvent) => {
            val text = AxisSize.text.apply.split(",")
            if (2 < text.size) {
              val res = text.map(_.toInt)
              yAxis = new NumberAxis(res(0), res(1), res(2))
              updateAll
            }
          }
        }

        SimpleX.onAction = (event: ActionEvent) => {
          val AxisSize = new TextField
          AxisSize.prefWidth = 550
          AxisSize.promptText = "Change size of X-Axis for Simple Regression. Write in the form:  UpperBound, lowerBound, tickSize   Hit enter to confirm"

          menuPane.children = Iterable(menuBar, label, segmentsText, AxisSize)

          AxisSize.onAction = (event: ActionEvent) => {
            val text = AxisSize.text.apply.split(",")
            if (2 < text.size) {
              val res = text.map(_.toInt)
              xAxis2 = new NumberAxis(res(0), res(1), res(2))
              updateAll
            }
          }
        }

        SimpleY.onAction = (event: ActionEvent) => {
          val AxisSize = new TextField
          AxisSize.prefWidth = 550
          AxisSize.promptText = "Change size of Y-Axis for Simple Regression. Write in the form:  UpperBound, lowerBound, tickSize   Hit enter to confirm"

          menuPane.children = Iterable(menuBar, label, segmentsText, AxisSize)

          AxisSize.onAction = (event: ActionEvent) => {
            val text = AxisSize.text.apply.split(",")
            if (2 < text.size) {
              val res = text.map(_.toInt)
              yAxis2 = new NumberAxis(res(0), res(1), res(2))
              updateAll
            }
          }
        }

        SegmentX.onAction = (event: ActionEvent) => {
          val AxisSize = new TextField
          AxisSize.prefWidth = 550
          AxisSize.promptText = "Change size of X-Axis for Segment Regression. Write in the form:  UpperBound, lowerBound, tickSize   Hit enter to confirm"

          menuPane.children = Iterable(menuBar, label, segmentsText, AxisSize)

          AxisSize.onAction = (event: ActionEvent) => {
            val text = AxisSize.text.apply.split(",")
            if (2 < text.size) {
              val res = text.map(_.toInt)
              xAxis3 = new NumberAxis(res(0), res(1), res(2))
              updateAll
            }
          }
        }

        SegmentY.onAction = (event: ActionEvent) => {
          val AxisSize = new TextField
          AxisSize.prefWidth = 550
          AxisSize.promptText = "Change size of Y-Axis for Segment Regression. Write in the form:  UpperBound, lowerBound, tickSize   Hit enter to confirm"

          menuPane.children = Iterable(menuBar, label, segmentsText, AxisSize)

          AxisSize.onAction = (event: ActionEvent) => {
            val text = AxisSize.text.apply.split(",")
            if (2 < text.size) {
              val res = text.map(_.toInt)
              yAxis3 = new NumberAxis(res(0), res(1), res(2))
              updateAll
            }
          }
        }

        //We can change how many lines the segment regression consists of
        segmentsText.onAction = (event: ActionEvent) => {
          val text = segmentsText.text.apply.toInt
          if (1 < text && text != dataPerLine) {
            //if(text>inputFile.pairs.size/11){
            dataPerLine = text // && text<pairs.size   maybe?
            updateAll
            //}
            //else label.text="Too few datapoints. Try more than : " + inputFile.pairs.size/11
          }
        }

      }
    }

    stage.setMaximized(true)
  }

  updateAll
}
