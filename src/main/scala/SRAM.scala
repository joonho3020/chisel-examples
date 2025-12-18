package example

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage
import java.io.PrintWriter

class OneReadOneWritePortSRAM(width: Int) extends Module {
  val io = IO(new Bundle {
    val ren = Input(Bool())
    val raddr = Input(UInt(3.W))
    val rdata = Output(Vec(4, UInt(width.W)))
    val wen = Input(Bool())
    val waddr = Input(UInt(3.W))
    val wdata = Input(Vec(4, UInt(width.W)))
    val wmask = Input(Vec(4, Bool()))
  })

  // Create a 32-bit wide memory that is byte-masked
  val mem = SyncReadMem(8, Vec(4, UInt(width.W)))
  when (io.wen) {
    mem.write(io.waddr, io.wdata, io.wmask)
  }
  io.rdata := mem.read(io.raddr, io.ren)
}


class SinglePortSRAM(width: Int) extends Module {
  val io = IO(new Bundle {
    val raddr = Input(UInt(3.W))
    val rdata = Output(Vec(4, UInt(width.W)))

    val wen = Input(Bool())
    val waddr = Input(UInt(3.W))
    val wdata = Input(Vec(4, UInt(width.W)))
    val wmask = Input(Vec(4, Bool()))
  })

  val mem = SyncReadMem(8, Vec(4, UInt(width.W)))
  when (io.wen) {
    mem.write(io.waddr, io.wdata, io.wmask)
  }

  io.rdata := mem.read(io.raddr, !io.wen)
}

class AggregateBundle extends Bundle {
  val a = UInt(2.W)
  val b = SInt(3.W)
}

class AggregateSRAM(width: Int) extends Module {
  val io = IO(new Bundle {
    val raddr = Input(UInt(3.W))
    val rdata = Output(Vec(4, new AggregateBundle))

    val wen = Input(Bool())
    val waddr = Input(UInt(3.W))
    val wdata = Input(Vec(4, new AggregateBundle))
    val wmask = Input(Vec(4, Bool()))
  })

  val mem = SyncReadMem(8, Vec(4, new AggregateBundle))
  when (io.wen) {
    mem.write(io.waddr, io.wdata, io.wmask)
  }

  io.rdata := mem.read(io.raddr, !io.wen)
}

class DualReadSingleWritePortSRAM(width: Int) extends Module {
  val io = IO(new Bundle {
    val raddr_0 = Input(UInt(3.W))
    val raddr_1 = Input(UInt(3.W))
    val rdata_0 = Output(Vec(4, UInt(width.W)))
    val rdata_1 = Output(Vec(4, UInt(width.W)))

    val wen = Input(Bool())
    val waddr = Input(UInt(3.W))
    val wdata = Input(Vec(4, UInt(width.W)))
    val wmask = Input(Vec(4, Bool()))
  })

  val mem = SyncReadMem(8, Vec(4, UInt(width.W)))
  when (io.wen) {
    mem.write(io.waddr, io.wdata, io.wmask)
  }

  io.rdata_0 := mem.read(io.raddr_0, !io.wen)
  io.rdata_1 := mem.read(io.raddr_1, !io.wen)
}

class OneReadOneReadWritePortSRAM(width: Int) extends Module {
  val io = IO(new Bundle {
    val raddr_0 = Input(UInt(3.W))
    val raddr_1 = Input(UInt(3.W))
    val rdata_0 = Output(Vec(4, UInt(width.W)))
    val rdata_1 = Output(Vec(4, UInt(width.W)))
    val ren = Input(Bool())

    val wen = Input(Bool())
    val waddr = Input(UInt(3.W))
    val wdata = Input(Vec(4, UInt(width.W)))
    val wmask = Input(Vec(4, Bool()))
  })

  val mem = SyncReadMem(8, Vec(4, UInt(width.W)))
  when (io.wen) {
    mem.write(io.waddr, io.wdata, io.wmask)
  }

  io.rdata_0 := mem.read(io.raddr_0, !io.wen)
  io.rdata_1 := mem.read(io.raddr_1, !io.wen && !io.ren)
}

class SRAMTestBundle extends Bundle {
  val a = UInt(4.W)
  val b = Bool()
}

class TopSRAMModule extends Module {
  // Declare a 2 read, 2 write, 2 read-write ported SRAM with 8-bit UInt data members
  val mem = SRAM(1024, UInt(8.W), 2, 1, 1)

  // Whenever we want to read from the first read port
  mem.readPorts(0).address := 100.U
  mem.readPorts(0).enable := true.B

  mem.readPorts(1).address := 102.U
  mem.readPorts(1).enable := true.B

  // Read data is returned one cycle after enable is driven
  val foo = WireInit(UInt(8.W), mem.readPorts(0).data)

  // Whenever we want to write to the second write port
  mem.writePorts(0).address := 5.U
  mem.writePorts(0).enable := true.B
  mem.writePorts(0).data := 12.U

  // Whenever we want to read or write to the third read-write port
  // Write:
  mem.readwritePorts(0).address := 5.U
  mem.readwritePorts(0).enable := true.B
  mem.readwritePorts(0).isWrite := true.B
  mem.readwritePorts(0).writeData := 100.U

  // Read:
  mem.readwritePorts(0).address := 5.U
  mem.readwritePorts(0).enable := true.B
  mem.readwritePorts(0).isWrite := false.B
  val bar = WireInit(UInt(8.W), mem.readwritePorts(0).readData)

  val mask = Wire(Vec(4, Bool()))
  mask(0) := true.B
  mask(1) := false.B
  mask(2) := true.B
  mask(3) := false.B

  val mem_2 = SRAM.masked(1024, Vec(4, new SRAMTestBundle), 1, 1, 1)

  mem_2.readPorts(0).address := 10.U
  mem_2.readPorts(0).enable := true.B

  mem_2.writePorts(0).address := 5.U
  mem_2.writePorts(0).enable := true.B

  mem_2.writePorts(0).data(0).a := 0.U
  mem_2.writePorts(0).data(0).b := true.B
  mem_2.writePorts(0).data(1).a := 1.U
  mem_2.writePorts(0).data(1).b := false.B
  mem_2.writePorts(0).data(2).a := 2.U
  mem_2.writePorts(0).data(2).b := true.B
  mem_2.writePorts(0).data(3).a := 3.U
  mem_2.writePorts(0).data(3).b := false.B

  mem_2.writePorts(0).mask.foreach(_ := mask)

  mem_2.readwritePorts(0).address := 5.U
  mem_2.readwritePorts(0).enable := true.B
  mem_2.readwritePorts(0).isWrite := true.B
  mem_2.readwritePorts(0).writeData(0).a := 0.U
  mem_2.readwritePorts(0).writeData(0).b := true.B
  mem_2.readwritePorts(0).writeData(1).a := 1.U
  mem_2.readwritePorts(0).writeData(1).b := false.B
  mem_2.readwritePorts(0).writeData(2).a := 2.U
  mem_2.readwritePorts(0).writeData(2).b := true.B
  mem_2.readwritePorts(0).writeData(3).a := 3.U
  mem_2.readwritePorts(0).writeData(3).b := false.B
  mem_2.readwritePorts(0).mask.foreach(_ := mask)
}
