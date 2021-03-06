package org.endmyopia.calc.measure

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_measure.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.endmyopia.calc.R
import org.endmyopia.calc.data.AppDatabase
import org.endmyopia.calc.data.Measurement
import org.endmyopia.calc.data.MeasurementMode
import org.endmyopia.calc.databinding.FragmentMeasureBinding
import org.endmyopia.calc.util.debug

class MeasureFragment : Fragment() {

    private lateinit var dataBinding: FragmentMeasureBinding
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_measure, container, false)

        dataBinding = FragmentMeasureBinding.bind(view)
        dataBinding.lifecycleOwner = this
        val holder: MeasureStateHolder =
            ViewModelProvider(activity!!).get(MeasureStateHolder::class.java)
        dataBinding.holder = holder

        return view
    }

    override fun onStart() {
        super.onStart()
        mediaPlayer = MediaPlayer.create(context, R.raw.dingaling)

        camera.setOnClickListener {
            if (dataBinding.holder?.hasTakenMeasurement?.value!!)
                dataBinding.holder?.hasTakenMeasurement?.postValue(false)
            else
                takeMeasurement()
        }

        delete.setOnClickListener {
            deleteMeasurement()
        }

        leftEye.setOnClickListener(
            getEyeModeChangeFn(
                MeasurementMode.LEFT,
                R.string.left_eye
            )
        )
        rightEye.setOnClickListener(
            getEyeModeChangeFn(
                MeasurementMode.RIGHT,
                R.string.right_eye
            )
        )
        bothEyes.setOnClickListener(
            getEyeModeChangeFn(
                MeasurementMode.BOTH,
                R.string.both_eyes
            )
        )
    }

    private fun getEyeModeChangeFn(mode: MeasurementMode, @StringRes resId: Int): (View) -> Unit {
        return {
            dataBinding.holder?.mode?.postValue(mode)
            Toast.makeText(
                context,
                resources.getString(R.string.measuring, resources.getString(resId)),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun ding() {
        if (!mediaPlayer.isPlaying) {
            //mediaPlayer.start()
        }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    fun update(distMeters: Double) {
        dataBinding.holder?.update(distMeters)
    }

    fun takeMeasurement() {
        dataBinding.holder?.hasTakenMeasurement?.postValue(true)
        activity?.let {
            GlobalScope.launch {
                val id = AppDatabase.getInstance(it.application).getMeasurementDao().insert(
                    Measurement(
                        0L,
                        dataBinding.holder?.mode?.value ?: MeasurementMode.BOTH,
                        System.currentTimeMillis(),
                        dataBinding.holder?.distanceMetersVal?.value ?: 0.0,
                        0.0
                    )
                )
                debug("measurementId: $id")
                dataBinding.holder?.lastPersistedMeasurementId = id
            }
            ding()
        }
    }

    private fun deleteMeasurement() {
        Toast.makeText(context, R.string.deleted, Toast.LENGTH_SHORT).show()
        dataBinding.holder?.hasTakenMeasurement?.postValue(false)
        activity?.let { activity ->
            dataBinding.holder?.let { holder ->
                GlobalScope.launch {
                    AppDatabase.getInstance(activity.application).getMeasurementDao().deleteById(
                        holder.lastPersistedMeasurementId
                    )
                }

            }
        }
    }
}
