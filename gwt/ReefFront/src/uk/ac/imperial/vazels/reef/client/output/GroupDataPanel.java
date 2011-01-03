package uk.ac.imperial.vazels.reef.client.output;

import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author simon
 *  
 * This class populates the data fields of a panel displaying information
 * about a particular group 
 *
 */

public class GroupDataPanel extends Composite {

  private static GroupDataPanelUiBinder uiBinder = GWT
      .create(GroupDataPanelUiBinder.class);
 
  @UiField Label group_name;
  @UiField Label workload;
  @UiField Label sue;
  @UiField Label size;
  @UiField FlexTable connected_vazels;
  @UiField FlexTable evolving_vazels;
  @UiField FlexTable variable_names;
  
  private String name = "";
  private String assigned_workload = "";
  private String assigned_sue = "";
  private int total_size = 0;

  interface GroupDataPanelUiBinder extends UiBinder<Widget, GroupDataPanel> {
  }

  public GroupDataPanel() {
    initWidget(uiBinder.createAndBindUi(this));
  }


  public GroupDataPanel(String firstName) {
    initWidget(uiBinder.createAndBindUi(this));
  }
  
  public void setGroupName(String groupName) {
    name = groupName;
    updateUI();
  }
  
  public void setWorkload(String workload) {
    assigned_workload = workload;
    updateUI();
  }

  public void setSUE(String sue) {
    assigned_sue  = sue;
    updateUI();
  }
  
  public void setConnectedHosts(String[] connectedHosts) {
    
    connected_vazels.removeAllRows();
    int i = 0;
    for(String vazel : connectedHosts) {
      
      Label vazelName = new Label(vazel);
      
      connected_vazels.setWidget(i, 0, vazelName);
      i++;
    }
    
    updateUI();
  }
  
  public void setVariables(Set<String> variableNames) {
    
    variable_names.removeAllRows();
    int i = 0;
    for(String vazel : variableNames) {
      
      Label variableName = new Label(vazel);
      
      variable_names.setWidget(i, 0, variableName);
      i++;
    }
    
    updateUI();
    
    
  }
  
  public void setEvolvingHosts(String[] evolvingHosts) {
    
    evolving_vazels.removeAllRows();
    int i = 0;
    for(String vazel : evolvingHosts) {
      
      Label vazelName = new Label(vazel);
      
      evolving_vazels.setWidget(i, 0, vazelName);
      i++;
    }
    
    updateUI();
  }
  

  private void updateUI() {
    group_name.setText(name);
    workload.setText(assigned_workload);
    sue.setText(assigned_sue);
    size.setText(Integer.toString(total_size));
  }


  public void setSize(int size) {
    total_size = size;
    updateUI();
  }


}
