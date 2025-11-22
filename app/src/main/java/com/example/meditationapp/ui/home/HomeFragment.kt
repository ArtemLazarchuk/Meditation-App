package com.example.meditationapp.ui.home

import android.content.*
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.meditationapp.R
import com.example.meditationapp.databinding.FragmentHomeBinding
import com.example.meditationapp.service.MeditationTimerService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.os.IBinder
import androidx.fragment.app.viewModels

class HomeFragment : Fragment() {

    private var _vb: FragmentHomeBinding? = null
    private val vb get() = _vb!!

    private val vm: HomeViewModel by viewModels()

    private var service: MeditationTimerService? = null
    private var bound = false
    private var selectedMinutes = 5

    private var mediaPlayer: MediaPlayer? = null
    private var currentSoundResId: Int = 0

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val local = binder as MeditationTimerService.LocalBinder
            service = local.getService()
            bound = true
            observeService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
            service = null
        }
    }

    private val finishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MeditationTimerService.ACTION_SESSION_FINISHED) {

                val duration = intent.getIntExtra(MeditationTimerService.EXTRA_DURATION_SECONDS, 0)
                val finishedAt = intent.getLongExtra(
                    MeditationTimerService.EXTRA_FINISHED_AT_MILLIS,
                    System.currentTimeMillis()
                )

                vm.saveSession(duration, finishedAt)

                Toast.makeText(context, "Сесія завершена: ${duration / 60} хв", Toast.LENGTH_SHORT).show()

                stopSound()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _vb = FragmentHomeBinding.inflate(inflater, container, false)
        return vb.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        val intent = Intent(requireContext(), MeditationTimerService::class.java)
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        requireContext().registerReceiver(
            finishedReceiver,
            IntentFilter(MeditationTimerService.ACTION_SESSION_FINISHED),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStop() {
        super.onStop()

        if (bound) {
            try {
                requireContext().unbindService(serviceConnection)
            } catch (_: Exception) {}
            bound = false
        }

        try {
            requireContext().unregisterReceiver(finishedReceiver)
        } catch (_: Exception) {}

        stopSound()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vb = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupTimeButtons()
        setupSoundSpinner()
        setupControls()
        observeStats()   // ← запускається ОДИН РАЗ
    }

    private fun setupControls() {
        vb.btnStart.setOnClickListener {
            val seconds = selectedMinutes * 60L

            if (bound) {
                service?.startTimer(seconds)
            } else {
                val intent = Intent(requireContext(), MeditationTimerService::class.java)
                requireContext().startService(intent)
                requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                service?.startTimer(seconds)
            }

            startSound()
        }

        vb.btnPause.setOnClickListener {
            if (bound) {
                service?.pauseTimer()
                pauseSound()
            }
        }

        vb.btnResume.setOnClickListener {
            if (bound) {
                service?.resumeTimer()
                resumeSound()
            }
        }

        vb.btnStop.setOnClickListener {
            if (bound) {
                service?.stopTimer()
                stopSound()

                val elapsed = service?.secondsLeft?.value?.let { left ->
                    (selectedMinutes * 60) - left
                } ?: 0

                if (elapsed > 5) {
                    vm.saveSession(elapsed.toInt(), System.currentTimeMillis())
                }
            }
        }
    }

    private fun observeService() {
        lifecycleScope.launch {
            while (!bound) {
                kotlinx.coroutines.delay(100)
            }
            service?.secondsLeft?.collectLatest { sec ->
                vb.tvTimer.text = formatTime(sec)
            }
        }
    }

    private fun observeStats() {
        lifecycleScope.launch {
            vm.totalSessions.collectLatest {
                vb.tvTotalSessions.text = "Всього сесій: $it"
            }
        }

        lifecycleScope.launch {
            vm.totalDuration.collectLatest { seconds ->
                val min = (seconds ?: 0) / 60
                vb.tvTotalTime.text = "Сумарний час: $min хв"
            }
        }

        lifecycleScope.launch {
            vm.streak.collectLatest { days ->
                vb.tvStreak.text = "Серія: $days днів"
            }
        }
    }

    private fun formatTime(sec: Long): String {
        val hours = sec / 3600
        val minutes = (sec % 3600) / 60
        val seconds = sec % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun setupTimeButtons() {
        val map = mapOf(
            vb.time3 to 3,
            vb.time5 to 5,
            vb.time10 to 10,
            vb.time20 to 20,
            vb.time30 to 30
        )

        fun updateUI(selectedView: View) {
            map.keys.forEach { it.setBackgroundResource(R.drawable.time_option_bg) }
            selectedView.setBackgroundResource(R.drawable.time_option_bg)
        }

        map.forEach { (view, minutes) ->
            view.setOnClickListener {
                selectedMinutes = minutes
                updateUI(view)
            }
        }

        updateUI(vb.time5)
    }

    private fun setupSoundSpinner() {
        val sounds = listOf(
            "Relax Ambience" to R.raw.ambiant_relax_sounds_10621,
            "Meditation Flow" to R.raw.meditation_flow_30s_307996,
            "Music For Meditation" to R.raw.music_for_meditation_20534,
            "New Composition" to R.raw.new_composition_3_20536,
            "Perfect Beauty" to R.raw.perfect_beauty_191271,
            "Piano Moment" to R.raw.piano_moment_9835,
            "Relaxing Sound" to R.raw.relaxing_145038
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            sounds.map { it.first }
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        vb.spinnerSound.adapter = adapter

        vb.spinnerSound.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                currentSoundResId = sounds[pos].second
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                currentSoundResId = sounds[0].second
            }
        }

        currentSoundResId = sounds[0].second
    }

    private fun startSound() {
        stopSound()
        mediaPlayer = MediaPlayer.create(requireContext(), currentSoundResId)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    private fun pauseSound() {
        mediaPlayer?.pause()
    }

    private fun resumeSound() {
        mediaPlayer?.start()
    }

    private fun stopSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
