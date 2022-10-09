package online.dailyq.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import online.dailyq.databinding.FragmentTimelineBinding
import online.dailyq.ui.base.BaseFragment

class TimelineFragment : BaseFragment() {

    var _binding: FragmentTimelineBinding? = null
    val binding get() = _binding!!
    lateinit var adapter: TimelineAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            adapter = TimelineAdapter(requireContext())
            adapter.addLoadStateListener {
                if (it.mediator?.refresh is LoadState.NotLoading) {
                    binding.refreshLayout.isRefreshing = false
                }
            }

            recycler.adapter = adapter.withLoadStateFooter(TimelineLoadStateAdapter {
                adapter.retry()
            })

            recycler.adapter = adapter
            recycler.layoutManager = LinearLayoutManager(context)

            binding.refreshLayout.setOnRefreshListener {
                lifecycleScope.launch {
                    delay(1000)
                    adapter.refresh()
                }
            }
        }

        lifecycleScope.launch {
            @OptIn(ExperimentalPagingApi::class)
            Pager(
                PagingConfig(initialLoadSize = 6, pageSize = 3, enablePlaceholders = false),
                null,
                TimelineRemoteMediator(api, db)
            ) {
                db.getQuestionDao().getPagingSource()
            }.flow.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
