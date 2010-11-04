package uk.ac.imperial.vazels.reef.client.settings.overlay;

import java.util.Iterator;


public class SectionList implements Iterable<String>
{
	private JsSectionList list;
	
	public SectionList(String content)
	{
		list = parse(content);
	}
	
	private native JsSectionList parse(String content)
	/*-{
		return eval(content);
	}-*/;

	@Override
  public Iterator<String> iterator()
  {
	  return new Iterator<String>()
	  {
	  	private int next = 0;
	  	
			@Override
      public boolean hasNext()
      {
				return next < list.length();
      }

			@Override
      public String next()
      {
				return list.sectionAt(next++);
      }

			@Override
      public void remove()
      {
      }
		};
  }
}
