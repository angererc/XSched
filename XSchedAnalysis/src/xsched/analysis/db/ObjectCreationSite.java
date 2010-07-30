package xsched.analysis.db;

import com.ibm.wala.ssa.SSANewInstruction;

public abstract class ObjectCreationSite {

	public static class SpecialCreationSite extends ObjectCreationSite {
		private final Object value;
		
		public SpecialCreationSite(Object value) {
			this.value = value;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof SpecialCreationSite) {
				SpecialCreationSite other = (SpecialCreationSite)o;
				return other.value.equals(value);
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			return value.hashCode();
		}
		
		@Override
		public String toString() {
			return value.toString();
		}
	}
	
	public static class SSANewInstructionCreationSite extends ObjectCreationSite {
		private final SSANewInstruction instruction;
		
		public SSANewInstructionCreationSite(SSANewInstruction instruction) {
			this.instruction = instruction;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof SSANewInstructionCreationSite) {
				SSANewInstructionCreationSite other = (SSANewInstructionCreationSite)o;
				return other.instruction.equals(instruction);
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			return instruction.hashCode();
		}
		
		@Override
		public String toString() {
			return instruction.toString();
		}
	}
}
