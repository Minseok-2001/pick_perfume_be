package ym_cosmetic.pick_perfume_be.batch.reader

import com.opencsv.bean.CsvToBeanBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.stereotype.Component
import ym_cosmetic.pick_perfume_be.batch.dto.PerfumeImportDto
import java.io.BufferedReader
import java.io.FileReader
import java.io.InputStreamReader
import org.springframework.batch.item.ItemReader
import org.springframework.core.io.Resource

@Component
class PerfumeCsvReader {
    
    fun createReader(csvFilePath: String): ItemReader<PerfumeImportDto> {
        val resource = if (csvFilePath.startsWith("classpath:")) {
            ClassPathResource(csvFilePath.substring("classpath:".length))
        } else {
            FileSystemResource(csvFilePath)
        }
        
        return PerfumeCsvItemReader(resource)
    }
    
    class PerfumeCsvItemReader(private val resource: Resource) : ItemReader<PerfumeImportDto> {
        private var perfumeIterator: Iterator<PerfumeImportDto>? = null
        
        override fun read(): PerfumeImportDto? {
            if (perfumeIterator == null) {
                perfumeIterator = initializeIterator()
            }
            
            return if (perfumeIterator!!.hasNext()) {
                perfumeIterator!!.next()
            } else {
                null // End of data
            }
        }
        
        private fun initializeIterator(): Iterator<PerfumeImportDto> {
            val inputStream = resource.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            
            val csvToBean = CsvToBeanBuilder<PerfumeImportDto>(reader)
                .withType(PerfumeImportDto::class.java)
                .withIgnoreLeadingWhiteSpace(true)
                .withSeparator(',')
                .build()
                
            return csvToBean.parse().iterator()
        }
    }
} 