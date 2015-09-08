package com.mdground.screen.activity;

import org.apache.http.Header;
import org.mdground.api.base.PlatformType;
import org.mdground.api.base.RequestCallBack;
import org.mdground.api.base.ResponseData;
import org.mdground.api.bean.Device;
import org.mdground.api.bean.Employee;
import org.mdground.api.server.global.LoginEmployee;
import org.mdground.api.server.global.UpdateDeviceToken;
import org.mdground.api.utils.DeviceUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.mdground.screen.MedicalAppliction;
import com.mdground.screen.R;
import com.mdground.screen.constant.MemberConstant;
import com.mdground.screen.utils.L;
import com.mdground.screen.utils.NetworkStatusUtil;
import com.mdground.screen.utils.PreferenceUtils;
import com.mdground.screen.utils.SharedPreferUtils;
import com.mdground.screen.utils.SharedPreferUtils.ShareKey;
import com.mdground.screen.view.ResizeLayout;
import com.mdground.screen.view.ResizeLayout.OnResizeListener;
import com.tencent.android.tpush.XGPushConfig;

public class LoginActivity extends Activity implements OnClickListener,
		OnResizeListener {

	private ResizeLayout LoginRootLayout;
	private ScrollView scrollView;
	private EditText et_account, et_password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		findViewById();
		setListener(); 

		// 自动登录
		String username = PreferenceUtils.getPrefString(LoginActivity.this,
				MemberConstant.USERNAME, null);
		String password = PreferenceUtils.getPrefString(LoginActivity.this,
				MemberConstant.PASSWORD, null);

		if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
			login(username, password);
		}
		
		PreferenceUtils.setPrefInt(getApplicationContext(),
				MemberConstant.LOGIN_STATUS,
				MemberConstant.LOGIN_OUT);
	}

	private void findViewById() {
		LoginRootLayout = (ResizeLayout) this
				.findViewById(R.id.login_root_layout);
		scrollView = (ScrollView) this.findViewById(R.id.scrollView);
		et_account = (EditText) findViewById(R.id.et_account);
		et_password = (EditText) findViewById(R.id.et_password);

		String username = PreferenceUtils.getPrefString(this,
				MemberConstant.USERNAME, "");
		if (username != null) {
			et_account.setText(username);
		}
	}

	private void setListener() {
		LoginRootLayout.setOnResizeListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {

		case R.id.btn_login:
			
			if (!NetworkStatusUtil.isConnected(this)) {
				Toast.makeText(this, "当前网络不可用，请检查网络设置", Toast.LENGTH_SHORT)
						.show();
				return;
			}

			String acccount = et_account.getText().toString().trim();
			String password = et_password.getText().toString().trim();

			if (TextUtils.isEmpty(acccount)) {
				Toast.makeText(getApplicationContext(), "请输入账号",
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (TextUtils.isEmpty(password)) {
				Toast.makeText(getApplicationContext(), "请输入密码",
						Toast.LENGTH_SHORT).show();
				return;
			}

			int deviceID = PreferenceUtils.getPrefInt(getApplicationContext(),
					MemberConstant.DEVICE_ID, -1);

			L.e(LoginActivity.this, "deviceID是 : " + deviceID);

			// login("vivi", "123");
			// login("2000078", "123");
			login(acccount, password);

			break;

		}
	}

	@Override
	public void OnResize(int w, final int h, int oldw, final int oldh) {
		L.e(LoginActivity.this, "OnResize()");
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {

			@Override
			public void run() {
				int offset = oldh - h;
				if (offset > 0) {
					scrollView.scrollTo(0, offset);
				}
			}
		});
	}

	private void login(final String userName, final String password) {

		Device device = DeviceUtils.getDeviceInfo(getApplicationContext());
		device.setPlatform(PlatformType.AndroidScreen.platform()); // 设死成3

		new LoginEmployee(this).loginEmployee(userName, password, device,
				new RequestCallBack() {

					@Override
					public void onSuccess(ResponseData response) {
						L.e(LoginActivity.this,
								"登录返回的信息是 : " + response.getContent());

						Employee employee = response.getContent(Employee.class);

						if (response.Code != 0) {
							Toast.makeText(getApplicationContext(),
									"账号异常,请联系客服", Toast.LENGTH_LONG).show();
							return;
						}

						((MedicalAppliction) LoginActivity.this
								.getApplication()).setLoginEmployee(employee);

						new SharedPreferUtils(getApplicationContext()).put(
								ShareKey.DEVICE_ID, employee.getDeviceID());

						PreferenceUtils.setPrefLong(getApplicationContext(),
								MemberConstant.LOGIN_EMPLOYEE,
								employee.getEmployeeID());
						PreferenceUtils.setPrefInt(getApplicationContext(),
								MemberConstant.LOGIN_STATUS,
								MemberConstant.LOGIN_IN);
						PreferenceUtils.setPrefString(getApplicationContext(),
								MemberConstant.USERNAME, employee.getLoginID());
						PreferenceUtils.setPrefString(getApplicationContext(),
								MemberConstant.PASSWORD, employee.getLoginPwd());
						PreferenceUtils.setPrefInt(getApplicationContext(),
								MemberConstant.DEVICE_ID,
								employee.getDeviceID());

						String token = XGPushConfig
								.getToken(getApplicationContext());
						L.e(LoginActivity.this, "token是 : " + token);

						int EmployeeRole = ((MedicalAppliction) LoginActivity.this
								.getApplication()).getLoginEmployee()
								.getEmployeeRole() & 8;

						L.e(LoginActivity.this,
								"getEmployeeRole : "
										+ ((MedicalAppliction) LoginActivity.this
												.getApplication())
												.getLoginEmployee()
												.getEmployeeRole());

						if (EmployeeRole != 0) {
							new UpdateDeviceToken(getApplicationContext())
									.updateDeviceToken(token,
											new RequestCallBack() {

												@Override
												public void onSuccess(
														ResponseData response) {
													L.e(LoginActivity.this,
															"code : "
																	+ response.Code);
													L.e(LoginActivity.this,
															"message : "
																	+ response.Message);
													L.e(LoginActivity.this,
															"上传token成功");
												}

												@Override
												public void onStart() {

												}

												@Override
												public void onFinish() {

												}

												@Override
												public void onFailure(
														int statusCode,
														Header[] headers,
														String responseString,
														Throwable throwable) {

												}
											});

							Intent intent = new Intent(LoginActivity.this,
									MainActivity.class);
							startActivity(intent);
						} else {
							Toast.makeText(getApplicationContext(),
									"账号异常,请联系客服", Toast.LENGTH_SHORT).show();
						}

					}

					@Override
					public void onStart() {
					}

					@Override
					public void onFinish() {
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						
						L.e(LoginActivity.this, "statusCode : " + statusCode);
						L.e(LoginActivity.this, "登录失败, 返回的responseString : " + responseString);
						L.e(LoginActivity.this, throwable.toString());
					}
				});
	}
}
