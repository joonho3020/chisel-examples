package example

import chisel3._
import chisel3.util._
import chisel3.util.Decoupled
import _root_.circt.stage.ChiselStage
import java.io.PrintWriter


class BundleQueue extends Bundle {
  val a = UInt(3.W)
  val b = UInt(2.W)
}

class MyQueue(length: Int) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new BundleQueue))
    val out = Decoupled(new BundleQueue)
  })
  val q = Module(new Queue(new BundleQueue, length))
  q.io.enq <> io.in
  io.out <> q.io.deq
}


class MyCustomQueue[T <: Data](data: T, entries: Int) extends Module {
  val io = IO(new Bundle {
    val enq = Flipped(Decoupled(data))
    val deq = Decoupled(data)
  })

  val addrBits = log2Ceil(entries + 1)
  val mem = Reg(Vec(entries, data))

  val enq_ptr = RegInit(0.U(addrBits.W))
  val deq_ptr = RegInit(0.U(addrBits.W))
  val full    = RegInit(false.B)
  val empty   = (enq_ptr === deq_ptr) && !full

  io.enq.ready := !full
  io.deq.valid := !empty
  io.deq.bits  := mem(deq_ptr)

  val enq_fire = io.enq.valid && io.enq.ready
  val deq_fire = io.deq.valid && io.deq.ready
  val almost_full = (enq_ptr + 1.U) % entries.U === deq_ptr

  when (enq_fire) {
    enq_ptr := (enq_ptr + 1.U) % entries.U
    mem(enq_ptr) := io.enq.bits
  }

  when (deq_fire) {
    deq_ptr := (deq_ptr + 1.U) % entries.U
  }

  when (enq_fire && deq_fire) {
  } .elsewhen (enq_fire && almost_full) {
    full := true.B
  } .elsewhen (deq_fire) {
    full := false.B
  }
}
