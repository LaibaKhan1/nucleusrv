package nucleusrv.components
import chisel3._
import chisel3.util._
import nucleusrv.components.fpu._

class ALU(F :Boolean) extends Module {
  val io = IO(new Bundle {
    val input1: UInt = Input(UInt(32.W))
    val input2: UInt = Input(UInt(32.W))
    val aluCtl: UInt = Input(UInt(5.W))

    //val input3 = if (F) Some(Input(UInt(32.W))) else None
    val rm = if (F) Some(Input(UInt(3.W))) else None

    val zero: Bool = Output(Bool())
    val result: UInt = Output(UInt(32.W))
  })

  val fConv = if (F) Some(Module(new Converter)) else None
  if (F) {
    Seq(
      (fConv.get.io.sIn, io.input1.asSInt),
      (fConv.get.io.uIn, io.input1),
      (fConv.get.io.roundMode, io.rm.get),
      (fConv.get.io.aluCtl, io.aluCtl)
    ).map(f => f._1 := f._2)
  }

  io.result := MuxCase((io.input1 & io.input2), Seq(
    (io.aluCtl === 1.U)  -> (io.input1 | io.input2),
    (io.aluCtl === 2.U)  -> (io.input1 + io.input2),
    (io.aluCtl === 3.U)  -> (io.input1 - io.input2),
    (io.aluCtl === 4.U)  -> (io.input1.asSInt < io.input2.asSInt).asUInt,
    (io.aluCtl === 5.U)  -> (io.input1 < io.input2),
    (io.aluCtl === 6.U)  -> (io.input1 << io.input2(4, 0)),
    (io.aluCtl === 7.U)  -> (io.input1 >> io.input2(4, 0)),
    (io.aluCtl === 8.U)  -> (io.input1.asSInt >> io.input2(4, 0)).asUInt,
    (io.aluCtl === 9.U)  -> (io.input1 ^ io.input2)) ++ (
      if (F) Seq(
        ((io.aluCtl >= 10.U) && (io.aluCtl <= 15.U)) -> fConv.get.io.uOut,
        (io.aluCtl === 16.U) -> fConv.get.io.sOut.asUInt
      ) else Seq()
    )
  )
  io.zero := DontCare
}
