package com.example.bottomnav

import android.annotation.SuppressLint
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnav.databinding.FragmentHomeBinding
import androidx.core.content.ContextCompat


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // This line is important to show the menu in a Fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_700))
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "Screen Time"

        usageStatsManager = context?.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        viewManager = LinearLayoutManager(context)
        viewAdapter = MyAdapter(emptyList())

        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        binding.getStatsButton.setOnClickListener {
            val usageStats = getUsageStats()
            displayUsageStats(usageStats)
        }

        binding.openSettingsButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reload -> {
                val usageStats = getUsageStats()
                displayUsageStats(usageStats)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getUsageStats(): List<UsageStats> {
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 1000 * 60 * 60 * 24 // 1 day

        val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime)
        return usageStatsList.filter { it.totalTimeInForeground > 0 }
    }

    private fun displayUsageStats(usageStats: List<UsageStats>) {
        viewAdapter = MyAdapter(usageStats)
        binding.recyclerView.adapter = viewAdapter
    }

    inner class MyAdapter(private val myDataset: List<UsageStats>) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

        inner class MyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): MyAdapter.MyViewHolder {
            val textView = TextView(parent.context)
            return MyViewHolder(textView)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val pm: PackageManager = context?.packageManager!!
            try {
                val applicationInfo = pm.getApplicationInfo(myDataset[position].packageName, 0)
                val appName = pm.getApplicationLabel(applicationInfo).toString()

                val totalSeconds = myDataset[position].totalTimeInForeground / 1000
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60

                holder.textView.text = "$appName: ${String.format("%d:%02d:%02d", hours, minutes, seconds)}"
            } catch (e: PackageManager.NameNotFoundException) {
                val totalSeconds = myDataset[position].totalTimeInForeground / 1000
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60

                holder.textView.text = "${myDataset[position].packageName}: ${String.format("%d:%02d:%02d", hours, minutes, seconds)}"
            }
        }

        override fun getItemCount() = myDataset.size
    }
}
