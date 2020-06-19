package com.pwrd.dls.marble.moudle.channel.ui

import androidx.lifecycle.*
import com.pwrd.dls.marble.common.base.BaseViewModel

@Suppress("UNCHECKED_CAST")
class GlobalData<T>(private val type: String) : BaseViewModel() {
    /**
     * 全局数据分享
     */
    companion object {
        @JvmStatic
        private val map = HashMap<String, DataWrapper<Any>>()

        @JvmStatic
        fun <T> get(viewModelStore: ViewModelStore, type: String): GlobalData<T> {
            return ViewModelProvider(viewModelStore, object : ViewModelProvider.Factory {
                override fun <V : ViewModel?> create(modelClass: Class<V>): V {
                    return GlobalData<T>(type) as V
                }
            }).get(GlobalData::class.java) as GlobalData<T>
        }
    }

    init {
        if (map[type] == null) {
            map[type] = DataWrapper<T>(type) as DataWrapper<Any>
        }
        map[type]?.addReference()
    }

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        (map[type]?.value as? MutableLiveData<T>)?.observe(lifecycleOwner, observer)
    }

    fun setValue(value: T) {
        map[type]?.value?.value = value
    }

    fun getData(): T? {
        return map[type]?.value?.value as? T
    }

    override fun onCleared() {
        super.onCleared()
        map[type]?.reduceReference()
    }

    class DataWrapper<T>(private val type: String) {
        var value: MutableLiveData<T> = MutableLiveData()
        private var alive: Int = 0
        fun addReference() {
            synchronized(alive) {
                alive++
            }
        }

        fun reduceReference() {
            synchronized(alive) {
                alive--
                if (alive <= 0) {
                    map.remove(type)
                }
            }
        }
    }
}