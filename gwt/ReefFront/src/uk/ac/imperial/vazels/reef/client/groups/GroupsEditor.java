package uk.ac.imperial.vazels.reef.client.groups;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.RequestHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class GroupsEditor extends Composite {
  private FlexTable mGroupDataTable;

  public GroupsEditor() {
    
    VerticalPanel verticalPanel = new VerticalPanel();
    initWidget(verticalPanel);
    
    Button refreshButton = new Button("Refresh Data");
    refreshButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        refresh();
      }
    });
    
    mGroupDataTable = new FlexTable();
    verticalPanel.add(mGroupDataTable);
    verticalPanel.add(refreshButton);
  }
  
  private void refresh() {
    new GroupDataRequest().go(new RequestHandler<GroupSummary>(){

      @Override
      public void handle(GroupSummary reply, boolean success, String message) {
        if (success) {
          dataReceived(reply);
        }
      }
    });
  }
  
  private void dataReceived(final GroupSummary incomingGroupData) {
    mGroupDataTable.clear();
    for (String key : incomingGroupData.keySet()) {
      String groupName = key;
      int groupSize = incomingGroupData.get(key);
      int bottomRow = mGroupDataTable.getRowCount();
      mGroupDataTable.setWidget(bottomRow, 0, new Label(groupName));
      mGroupDataTable.setWidget(bottomRow, 1, new Label(Integer.toString(groupSize)));
    }
  }
  
  
  private class GroupDataRequest extends MultipleRequester<GroupSummary>{
    GroupDataRequest() {
      super(RequestBuilder.GET, "/groups/", 
          new Converter<GroupSummary>() {

            @Override
            public GroupSummary convert(String original) {
              return new GroupSummary(original);
            }
        
      });
    }
  }

}
