package com.mdground.screen;

import org.mdground.api.MdgAppliction;
import org.mdground.api.base.PlatformType;
import org.mdground.api.bean.Employee;

import com.iflytek.cloud.SpeechUtility;
import com.mdground.screen.constant.MemberConstant;
import com.mdground.screen.utils.L;
import com.mdground.screen.utils.MedicalImageDownload;
import com.mdground.screen.utils.PreferenceUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.XGPushService;

import android.content.Context;
import android.content.Intent;

public class MedicalAppliction extends MdgAppliction {

	public static Employee employee;// 登陆用户

	@Override
	public void onCreate() {
		super.onCreate();

		// 开启logcat输出，方便debug，发布时请关闭
		XGPushConfig.enableDebug(this, false);
		// 如果需要知道注册是否成功，请使用registerPush(getApplicationContext(),
		// XGIOperateCallback)带callback版本
		// 如果需要绑定账号，请使用registerPush(getApplicationContext(),account)版本
		// 具体可参考详细的开发指南
		// 传递的参数为ApplicationContext
		Context context = getApplicationContext();
		// XGPushManager.registerPush(context, new XGIOperateCallback() {
		//
		// @Override
		// public void onSuccess(Object arg0, int arg1) {
		// L.e(MedicalAppliction.this, "信鸽注册成功");
		// }
		//
		// @Override
		// public void onFail(Object arg0, int arg1, String arg2) {
		// L.e(MedicalAppliction.this, "信鸽注册失败");
		// }
		// });
		L.e(this, "MedicalAppliction");
		XGPushManager.registerPush(getApplicationContext());

		// 2.36（不包括）之前的版本需要调用以下2行代码
		Intent service = new Intent(context, XGPushService.class);
		context.startService(service);

		// 其它常用的API：
		// 绑定账号（别名）注册：registerPush(context,account)或registerPush(context,account,
		// XGIOperateCallback)，其中account为APP账号，可以为任意字符串（qq、openid或任意第三方），业务方一定要注意终端与后台保持一致。
		// 取消绑定账号（别名）：registerPush(context,"*")，即account="*"为取消绑定，解绑后，该针对该账号的推送将失效
		// 反注册（不再接收消息）：unregisterPush(context)
		// 设置标签：setTag(context, tagName)
		// 删除标签：deleteTag(context, tagName)

		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(getApplicationContext())
				.imageDownloader(new MedicalImageDownload(getApplicationContext())).build();
		ImageLoader.getInstance().init(configuration);
	}

	public MedicalAppliction() {
	}

	public Employee getLoginEmployee() {
		return employee;
	}

	public void setLoginEmployee(Employee employee) {
		this.employee = employee;
	}

	@Override
	public int getDeviceId() {
		return PreferenceUtils.getPrefInt(this, MemberConstant.DEVICE_ID, 0);
	}

	@Override
	public int getPlatform() {
		return PlatformType.AndroidScreen.platform();
	}

}
