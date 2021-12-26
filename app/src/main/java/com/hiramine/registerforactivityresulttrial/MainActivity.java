package com.hiramine.registerforactivityresulttrial;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

/*
Request app permissions
https://developer.android.com/training/permissions/requesting?hl
*/

public class MainActivity extends AppCompatActivity
{
	// ※ registerForActivityResult()は、アプリ開始前に実施する必要があり、メンバーとして定義する。（アプリ開始後にコールすると、「IllegalStateException」が発生する）
	// ランチャーの作成
	private final ActivityResultLauncher<String> m_launcher              = registerForActivityResult( new ActivityResultContracts.RequestPermission(),
																									  result -> onRequestPermissionsResult( result, false ) );
	// ランチャーの作成（説明ダイアログあり版）
	private final ActivityResultLauncher<String> m_launcherWithRationale = registerForActivityResult( new ActivityResultContracts.RequestPermission(),
																									  result -> onRequestPermissionsResult( result, true ) );

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		// 「Start」ボタンを押したときの処理の設定
		Button button = findViewById( R.id.button_start );
		button.setOnClickListener( view ->
								   {
									   // カメラ権限のリクエスト
									   boolean bPermissionGranted = requestCameraPermission();
									   if( bPermissionGranted )
									   {    // 権限は許可されている
										   // カメラを用いた処理の開始
										   startProcessUsingCamera();
									   }
								   } );
	}

	// カメラを用いた処理の開始
	private void startProcessUsingCamera()
	{
		Toast.makeText( this, R.string.message_start_process_using_camera, Toast.LENGTH_SHORT ).show();
	}

	// 「権限が拒否された」メッセージの表示
	private void showPermissionDeniedMessage()
	{
		Toast.makeText( this, R.string.message_permission_denied, Toast.LENGTH_SHORT ).show();
	}

	// カメラ権限のリクエスト
	private boolean requestCameraPermission()
	{
		if( PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission( this, Manifest.permission.CAMERA ) )
		{    // 権限は許可されている
			return true;
		}
		// 権限は許可されていない

		// 「権限が必要であることの説明ダイアログ」を表示すべきかどうかの確認
		//   shouldShowRequestPermissionRationale()は、
		//   - これまでに「権限リクエストダイアログ」を表示したことがない場合は、false(=説明ダイアログ表示は不要)が返る。
		//   - これまでに「権限リクエストダイアログ」を表示したことがあり、その際に「許可する」を選択した場合は、権限は許可されているので、ここに来ない。
		//   - これまでに「権限リクエストダイアログ」を表示したことがあり、その際に「許可しない」を選択し、かつ、「今後は確認しない」を「チェックした」場合は、false(=説明ダイアログ表示は不要)が返る。
		//   - これまでに「権限リクエストダイアログ」を表示したことがあり、その際に「許可しない」を選択し、かつ、「今後は確認しない」を「チェックしなかった」場合は、true(=説明ダイアログが必要)が返る。
		if( ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.CAMERA ) )
		{    // 「権限が必要であることの説明ダイアログ」を表示すべき
			// （これまでに「権限リクエストダイアログ」を表示したことがあり、その際に「許可しない」を選択し、かつ、「今後は確認しない」を「チェックしなかった」場合）

			// 「権限が必要であることの説明ダイアログ」を表示し、「OK」ボタンを押すと、「権限リクエストダイアログ」を表示
			new AlertDialog.Builder( this )
					.setTitle( R.string.app_name )
					.setMessage( R.string.message_request_permission_rationale )
					.setPositiveButton( android.R.string.ok,
										( dialog, which ) ->
												m_launcherWithRationale.launch( Manifest.permission.CAMERA ) )
					.show();
		}
		else
		{    // 「権限が必要であることの説明ダイアログ」を表示すべきでない
			// （これまでに「権限リクエストダイアログ」を表示したことがない場合、もしくは、
			// 　これまでに「権限リクエストダイアログ」を表示したことがあり、その際に「許可しない」を選択し、かつ、「今後は確認しない」を「チェックした」場合）

			// 「権限リクエストダイアログ」を表示
			m_launcher.launch( Manifest.permission.CAMERA );
		}

		// 「権限リクエストダイアログ」の結果は、onRequestPermissionsResult()で受け取る。
		// 本関数は、ひとまず「権限は許可されていない」を返す。
		return false;
	}

	// 「権限リクエストダイアログ」の結果
	public void onRequestPermissionsResult( boolean result, boolean bWithRationale )
	{
		if( result )
		{    // 権限は許可された
			// カメラを用いた処理の開始
			startProcessUsingCamera();
		}
		else
		{    // 権限は許可されなかった
			// もしくは、
			// これまでに「権限リクエストダイアログ」を表示したことがあり、その際に「許可しない」を選択し、かつ、「今後は確認しない」を「チェックした」場合、
			// launcher.launch()コール後、「権限リクエストダイアログ」は表示されず、ここに来る。

			// 「これまでに『許可しない』かつ『今後は確認しない』を選択していてここに来た」場合、「設定」アプリの「このアプリの詳細設定」へ誘導する。

			//   shouldShowRequestPermissionRationale()を使って、「これまでに『許可しない』かつ『今後は確認しない』を選択していてここに来た」かを判別する。
			//   - これまでに「権限リクエストダイアログ」を表示したことがない場合は、ここに来ない。（onRequestPermissionsResult()は、「権限リクエストダイアログ」の結果を受け取る関数なので）
			//   - これまでに「権限リクエストダイアログ」を表示したことがあり、その際に「許可する」を選択した場合は、権限は許可されているので、ここに来ない。
			//   - これまでに「権限リクエストダイアログ」を表示したことがあり、その際に「許可しない」を選択し、かつ、「今後は確認しない」を「チェックした」場合は、false(=説明ダイアログ表示は不要)が返る。
			//   - これまでに「権限リクエストダイアログ」を表示したことがあり、その際に「許可しない」を選択し、かつ、「今後は確認しない」を「チェックしなかった」場合は、true(=説明ダイアログが必要)が返る。
			if( bWithRationale
				|| ActivityCompat.shouldShowRequestPermissionRationale( this,
																		Manifest.permission.CAMERA ) )
			{
				// 「権限が必要であることの説明ダイアログ」を表示してここに来た場合は、「これまでに『許可しない』かつ『今後は確認しない』を選択していてここに来た」場合ではない
				// 「権限が必要であることの説明ダイアログ」を表示すべき場合は、「これまでに『許可しない』かつ『今後は確認しない』を選択していてここに来た」場合ではない

				// 「権限が拒否された」メッセージの表示
				showPermissionDeniedMessage();
			}
			else
			{
				// 「これまでに『許可しない』かつ『今後は確認しない』を選択していてここに来た」場合

				// 「権限が必要であることの説明と、このアプリの詳細設定画面を開くか」のダイアログを表示し、
				// 「OK」ボタンを押すと、「設定」アプリの「このアプリの詳細設定」を開く
				// 「No」ボタンを押すと、「権限が拒否された」メッセージを表示
				new AlertDialog.Builder( this )
						.setTitle( R.string.app_name )
						.setMessage( R.string.confirm_open_application_details_settings )
						.setPositiveButton( android.R.string.ok, ( dialog, which ) -> openApplicationDetailsSettings() )
						.setNegativeButton( android.R.string.no, ( dialog, which ) -> showPermissionDeniedMessage() )
						.show();
			}
		}
	}

	// 「設定」アプリの「このアプリの詳細設定」を開く
	private void openApplicationDetailsSettings()
	{
		Intent intent = new Intent( Settings.ACTION_APPLICATION_DETAILS_SETTINGS );
		Uri    uri    = Uri.fromParts( "package", getPackageName(), null );
		intent.setData( uri );
		startActivity( intent );
	}
}