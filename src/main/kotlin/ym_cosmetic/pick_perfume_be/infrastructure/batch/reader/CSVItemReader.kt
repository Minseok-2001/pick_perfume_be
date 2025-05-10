package ym_cosmetic.pick_perfume_be.infrastructure.batch.reader

import com.opencsv.bean.CsvToBeanBuilder
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import java.io.BufferedReader
import java.io.FileReader
import java.io.InputStreamReader

/**
 * CSV 파일을 읽어오는 ItemReader 팩토리 클래스
 */
object CSVItemReaderFactory {
    
    /**
     * 지정된 CSV 파일을 읽어오는 ItemReader를 생성합니다.
     * 
     * @param csvFile CSV 파일 경로
     * @param clazz 변환할 데이터 클래스 타입
     * @param headers CSV 파일의 헤더 목록
     * @param delimiter 구분자 (기본값: ',')
     * @return 생성된 ItemReader
     */
    inline fun <reified T> createReader(
        csvFile: String,
        headers: Array<String>,
        delimiter: Char = ','
    ): ItemReader<T> {
        val resource = try {
            ClassPathResource(csvFile)
        } catch (e: Exception) {
            FileSystemResource(csvFile)
        }
        
        val lineMapper = DefaultLineMapper<T>()
        val tokenizer = DelimitedLineTokenizer().apply {
            setDelimiter(delimiter.toString())
            setNames(*headers)
        }
        
        lineMapper.setLineTokenizer(tokenizer)
        
        // 각 라인을 T 타입으로 변환하는 로직
        val reader = FlatFileItemReader<T>()
        reader.setResource(resource)
        reader.setLineMapper(lineMapper)
        reader.setLinesToSkip(1) // 첫 번째 줄(헤더)은 건너뜁니다
        
        return reader
    }
    
    /**
     * OpenCSV를 사용하여 CSV 파일을 읽어오는 메서드입니다.
     * 
     * @param resource CSV 파일 리소스
     * @param clazz 변환할 데이터 클래스 타입
     * @return 읽어온 데이터 목록
     */
    inline fun <reified T> readWithOpenCSV(
        resource: Resource,
        skipHeader: Boolean = true
    ): List<T> {
        val reader = BufferedReader(InputStreamReader(resource.inputStream))
        
        val csvToBean = CsvToBeanBuilder<T>(reader)
            .withType(T::class.java)
            .withIgnoreLeadingWhiteSpace(true)
            .withSkipLines(if (skipHeader) 1 else 0)
            .build()
            
        return csvToBean.parse()
    }
} 