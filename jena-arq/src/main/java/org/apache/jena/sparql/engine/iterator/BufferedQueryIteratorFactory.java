package org.apache.jena.sparql.engine.iterator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.serializer.SerializationContext;

public class BufferedQueryIteratorFactory {

	private Vector<Binding> buffer = new Vector<Binding>();
	private final QueryIterator qIter;

	public BufferedQueryIteratorFactory(QueryIterator qIter) {
		this.qIter = qIter;
	}
	
	public BufferedQueryIterator createBufferedQueryIterator() {
		return new BufferedQueryIterator(qIter, buffer);
	}
	
	
	public class BufferedQueryIterator extends QueryIteratorBase{
		
		private QueryIterator qIter;
		private List<Binding> buffer;
		private int pos;

		private BufferedQueryIterator(QueryIterator qIter, List<Binding> buffer) {
			this.pos = 0;
			this.qIter = qIter;
			this.buffer = buffer;
		}

		@Override
		public void output(IndentedWriter out, SerializationContext sCxt) {}

		@Override
		protected boolean hasNextBinding() {
			boolean b =  pos < buffer.size() || qIter.hasNext();
			return b;
		}

		@Override
		protected Binding moveToNextBinding() {
			if(pos == buffer.size() && qIter.hasNext()) {
				buffer.add(qIter.nextBinding());
			}
			return buffer.get(pos++);
		}

		@Override
		protected void closeIterator() { qIter.close();}

		@Override
		protected void requestCancel() {}

		public Collection<Binding> consume() {
			List<Binding> allBindings = new LinkedList<>();
			while(this.hasNextBinding()) {
				allBindings.add(this.moveToNextBinding());
			}
			this.closeIterator();
			return allBindings;
		}

	}
	
}
