package com.pqixing.mvp.utils;

import com.pqixing.mvp.constract.BaseContract;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 创建presenter工具类
 * @author LB
 *
 */
@SuppressWarnings("unchecked")
public class PresenterInitUtil {
	
	/**
	 * 反射创建presenter
	 * @param currentClass
	 */
	public  static <P> P createPresenter(Object currentClass, Class<? extends BaseContract.IPresenter> presenterClazz) {
		if (currentClass == null) {
			return null;
		}

		try {
			P presenter = (P) presenterClazz.getConstructor().newInstance();
			Method[] methods = presenterClazz.getMethods();
			for(Method m : methods){
				if("setViewer".equals(m.getName())){
					m.invoke(presenter, currentClass);
				}
			}
			/*Field viewer = presenterClazz.getField("mViewer");
			viewer.setAccessible(true);
			viewer.set(presenter, currentClass);*/

			return presenter;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取父类泛型的类型
	 *
	 * @param o
	 * @return
	 */
	public static Class getGenericType(Object o, int i) {
		Type type = o.getClass().getGenericSuperclass();
		ParameterizedType p = (ParameterizedType) type;
		return (Class) p.getActualTypeArguments()[i];
	}
}
