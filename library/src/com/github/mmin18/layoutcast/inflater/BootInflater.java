package com.github.mmin18.layoutcast.inflater;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.view.LayoutInflater;

import com.github.mmin18.layoutcast.context.OverrideContext;

/**
 * Used to replace the application service, provide Activity's layout inflater
 * by cloneInContext()
 * 
 * @author mmin18
 */
public class BootInflater extends BaseInflater {

	/**
	 * The original LayoutInflater in Application Service
	 */
	public static LayoutInflater systemInflater;

	public BootInflater(Context context) {
		super(context);
	}

	@Override
	public LayoutInflater cloneInContext(Context newContext) {
		if (newContext instanceof ContextWrapper) {
			try {
				OverrideContext.overrideDefault((ContextWrapper) newContext);
			} catch (Exception e) {
				Log.e("lcast", "fail to override resource in context "
						+ newContext, e);
			}
		}
		return super.cloneInContext(newContext);
	}

	public static void initApplication(Application app) {
		LayoutInflater inflater = (LayoutInflater) app
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (inflater instanceof BootInflater) {
			// already inited
			return;
		}
		systemInflater = inflater;
		Class<?> cCtxImpl = app.getBaseContext().getClass();
		boolean ctxFetcherExisted,sysFetcherExisted;
		if ("android.app.ContextImpl".equals(cCtxImpl.getName())) {
			try {
				ClassLoader cl = cCtxImpl.getClassLoader();
				ctxFetcherExisted = CxtImpSFetcherExisted();
				sysFetcherExisted = SysRegSFetcherExisted();
				if (!ctxFetcherExisted && !sysFetcherExisted){return;}
				Class<?> cStaticFetcher = cl
						.loadClass(ctxFetcherExisted? "android.app.ContextImpl$StaticServiceFetcher"
								:"android.app.SystemServiceRegistry$StaticServiceFetcher");
				Class<?> cFetcherContainer = null;
				for (int i = 1; i < 50; i++) {
					String cn = ctxFetcherExisted ? ("android.app.ContextImpl$" + i):
							("android.app.SystemServiceRegistry$" + i);
					try {
						Class<?> c = cl.loadClass(cn);
						if (cStaticFetcher.isAssignableFrom(c)) {
							cFetcherContainer = c;
							break;
						}
					} catch (Exception e) {
					}
				}
				Constructor<?> cFetcherConstructor = cFetcherContainer
						.getDeclaredConstructor();
				cFetcherConstructor.setAccessible(true);
				Object fetcher = cFetcherConstructor.newInstance();
				Field f = cStaticFetcher.getDeclaredField("mCachedInstance");
				f.setAccessible(true);
				f.set(fetcher, new BootInflater(app));
				if (ctxFetcherExisted){
					f = cCtxImpl.getDeclaredField("SYSTEM_SERVICE_MAP");
				}
				else{
					Class<?> sysRegistry = cl.loadClass("android.app.SystemServiceRegistry");
					f = sysRegistry.getDeclaredField("SYSTEM_SERVICE_FETCHERS");
				}
				f.setAccessible(true);
				HashMap<String, Object> map = (HashMap<String, Object>) f
						.get(null);
				map.put(Context.LAYOUT_INFLATER_SERVICE, fetcher);
			} catch (Exception e) {
				throw new RuntimeException(
						"unable to initialize application for BootInflater" + e);
			}
		} else {
			throw new RuntimeException("application base context class "
					+ cCtxImpl.getName() + " is not expected");
		}

		if (!(app.getSystemService(Context.LAYOUT_INFLATER_SERVICE) instanceof BootInflater)) {
			throw new RuntimeException(
					"unable to initialize application for BootInflater");
		}
	}


	private static boolean CxtImpSFetcherExisted(){
		try{
			Class.forName("android.app.ContextImpl$StaticServiceFetcher");
			return true;
		} catch (ClassNotFoundException e){
			return false;
		}
	}
	private static boolean SysRegSFetcherExisted(){
		try{
			Class.forName("android.app.SystemServiceRegistry$StaticServiceFetcher");
			return true;
		} catch (ClassNotFoundException e){
			return false;
		}
	}
}
