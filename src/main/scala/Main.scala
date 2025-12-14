package example

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage
import java.io.PrintWriter

object Main extends App {
  def elaborate(gen: => RawModule, name: String): Unit = {
    val sv = ChiselStage.emitSystemVerilog(
      gen,
      firtoolOpts = Array(
        "-disable-all-randomization",
        "-strip-debug-info",
        "--lowering-options=disallowLocalVariables,noAlwaysComb,verifLabels,disallowPortDeclSharing",
      )
    )
    val svWriter = new PrintWriter(f"generated/$name.sv")
    svWriter.write(sv)
    svWriter.close()

    val chirrtl = ChiselStage.emitCHIRRTL(
      gen
    )
    val chirrtlWriter = new PrintWriter(f"generated/$name.fir")
    chirrtlWriter.write(chirrtl)
    chirrtlWriter.close()

// val lofirrtl = ChiselStage.em
  }

  elaborate(new Adder(2), "Adder")
  elaborate(new Subtracter(2), "Subtracter")
  elaborate(new Const(2), "Const")
  elaborate(new Cache, "Cache")

  elaborate(new DecoupledMux, "DecoupledMux")
  elaborate(new DynamicIndexing, "DynamicIndexing")

  elaborate(new Fir(4), "Fir")
  elaborate(new GCD, "GCD")
  elaborate(new GCDDelta, "GCDDelta")
  elaborate(new Top, "Hierarchy")
  elaborate(new NestedWhen, "NestedWhen")

  elaborate(new LCS1, "LCS1")
  elaborate(new LCS2, "LCS2")
  elaborate(new LCS3, "LCS3")
  elaborate(new LCS4, "LCS4")
  elaborate(new LCS5, "LCS5")
  elaborate(new LCS6, "LCS6")
  elaborate(new LCS7, "LCS7")
  elaborate(new LCS8, "LCS8")
// elaborate(new NestedBundleModule, "NestedBundleModule")
  elaborate(new WireRegInsideWhen, "WireRegInsideWhen")
  elaborate(new MultiWhen, "MultiWhen")

  elaborate(new BitSel1, "BitSel1")
  elaborate(new BitSel2, "BitSel2")

  elaborate(new PointerChasing, "PointerChasing")
  elaborate(new MyQueue(2), "MyQueue")
  elaborate(new RegFile, "RegFile")
  elaborate(new TestRegInit(2), "RegInit")
  elaborate(new RegVecInit(2), "RegVecInit")

  elaborate(new RegInitWire, "RegInitWire")

  elaborate(new SinglePortSRAM(2), "SinglePortSRAM")
  elaborate(new OneReadOneWritePortSRAM(2), "OneReadOneWritePortSRAM")
  elaborate(new AggregateSRAM(2), "AggregateSRAM")
  elaborate(new DualReadSingleWritePortSRAM(2), "DualReadSingleWritePortSRAM")
  elaborate(new OneReadOneReadWritePortSRAM(2), "OneReadOneReadWritePortSRAM")

  elaborate(new CombHierarchy, "CombHierarchy")

  elaborate(new AESStep1, "AESStep1")
  elaborate(new AESStep2, "AESStep2")
  elaborate(new AESStep3, "AESStep3")

  elaborate(new CordicStep1, "CordicStep1")
  elaborate(new CordicStep2, "CordicStep2")
  elaborate(new CordicStep3, "CordicStep3")

  elaborate(new FFTStep1, "FFTStep1")
  elaborate(new FFTStep2, "FFTStep2")
  elaborate(new FFTStep3, "FFTStep3")
  elaborate(new FFTStep4, "FFTStep4")

  elaborate(new MyCustomQueue(UInt(3.W), 4), "MyCustomQueue")
}
