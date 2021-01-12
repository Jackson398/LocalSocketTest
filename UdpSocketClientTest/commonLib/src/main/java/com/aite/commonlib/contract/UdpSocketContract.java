package com.aite.commonlib.contract;

import com.aite.commonlib.base.BasePresenter;
import com.aite.commonlib.base.BaseView;

public class UdpSocketContract {

    public interface Presenter extends BasePresenter {
        void startBuildLink();
        void buildLinkSuccess();
        void sendMessage(String msg);
        void recvMessage(String msg);
        void disconnect();
    }

    public interface View extends BaseView<Presenter> {
        void setPanelMsg(String appendMsg);

        void setBuildLinkBtnState(boolean enable);

        void closeProgressDlg();

        void onTimeOut();
    }
}
